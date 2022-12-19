package ru.netology.server;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import ru.netology.model.Request;
import ru.netology.utils.ResponseUtils;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

public class Processor implements Runnable {

    private static final byte[] REQUEST_LINE_DELIMITER = {'\r', '\n'};
    private static final byte[] HEADERS_DELIMITER = {'\r', '\n', '\r', '\n'};
    private static final String URLENCODED = "Content-Type: application/x-www-form-urlencoded";
    private Socket socket;
    private Map<String, Map<String, Handler>> handlers;
    private Request request;

    public Processor(Socket socket, Map<String, Map<String, Handler>> handlers) {
        this.socket = socket;
        this.handlers = handlers;
        request = new Request();
    }

    @Override
    public void run() {
        try (BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            processRequest(in, out);

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRequest(BufferedInputStream in, BufferedOutputStream out) throws IOException, URISyntaxException {
        // лимит на request line + заголовки
        int limit = 4096;
        in.mark(limit);
        byte[] buffer = new byte[limit];
        int read = in.read(buffer);

        int headersStart = processRequestLine(buffer, read, out);
        if (headersStart == -1) return;

        processRequestHeaders(buffer, read, headersStart, in, out);

        if (!processRequestHeaders(buffer, read, headersStart, in, out)) return;

        if (!request.getMethod().equals("GET")) {
            processRequestBody(in);
        }

        Handler handler = handlers.get(request.getMethod()).get(request.getPath());

        if (handler == null) {
            ResponseUtils.sendNotFound(out);
            System.out.println(request + " handled with 404 error!");
        } else {
            handler.handle(request, out);
            System.out.println(request + " handled!");
        }
    }

    private int processRequestLine(byte[] buffer, int read, BufferedOutputStream out) throws URISyntaxException {
        // ищем request line
        int requestLineEnd = indexOf(buffer, REQUEST_LINE_DELIMITER, 0, read);

        // если не нашел символов переноса строки (\r\n), значит запрос некорректный
        if (requestLineEnd == -1) {
            ResponseUtils.sendBadRequest(out);
            return -1;
        }

        String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            ResponseUtils.sendBadRequest(out);
            return -1;
        }

        String method = requestLine[0];
        if (!handlers.containsKey(method)) {
            ResponseUtils.sendMethodNotAllowed(out);
            return -1;
        }

        String uri = requestLine[1];
        if (!uri.startsWith("/")) {
            ResponseUtils.sendBadRequest(out);
            return -1;
        }

        String path;
        if (uri.contains("?")) {
            path = uri.substring(0, uri.indexOf('?'));
            List<NameValuePair> queryParams = URLEncodedUtils.parse(new URI(uri), Charset.defaultCharset());
            request.setQueryParams(queryParams);
        } else {
            path = uri;
        }

        request.setMethod(method);
        request.setPath(path);

        // возвращаем количество байтов строки запроса, включая \r\n
        return requestLineEnd + REQUEST_LINE_DELIMITER.length;
    }

    private boolean processRequestHeaders(byte[] buffer, int read, int headersStart,
                                          BufferedInputStream in, BufferedOutputStream out) throws IOException {
        final var headersEnd = indexOf(buffer, HEADERS_DELIMITER, headersStart, read);
        if (headersEnd == -1) {
            ResponseUtils.sendBadRequest(out);
            return false;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        byte[] headersBytes = in.readNBytes(headersEnd - headersStart);
        List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        request.setHeaders(headers);
        return true;
    }

    private void processRequestBody(BufferedInputStream in) throws IOException {
        in.skip(HEADERS_DELIMITER.length);
        // вычитываем Content-Length, чтобы прочитать body
        Optional<String> contentLength = extractHeader(request.getHeaders(), "Content-Length");
        if (contentLength.isPresent()) {
            int bodyLength = Integer.parseInt(contentLength.get());
            byte[] bodyBytes = in.readNBytes(bodyLength);
            String body = new String(bodyBytes);

            if (request.getHeaders().contains(URLENCODED)) {
                Map<String, List<String>> postParams = new HashMap<>();
                String[] params = body.split("&");
                for (String param : params) {
                    String[] prm = param.split("=");
                    if (!postParams.containsKey(prm[0])) {
                        postParams.put(prm[0], new ArrayList<>(List.of(prm[1])));
                    } else {
                        postParams.get(prm[0]).add(prm[1]);
                    }
                }
                request.setPostParams(postParams);
            }

            request.setBody(body);
        }


    }

    // from google guava with modifications
    private int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}
