package ru.netology.server;

import ru.netology.utils.TimeUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Processor implements Runnable {

    private Socket socket;
    private List<String> validPaths;

    public Processor(Socket socket, List<String> validPaths) {
        this.socket = socket;
        this.validPaths = validPaths;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            String request = in.readLine();
            processRequest(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRequest(String request, BufferedOutputStream out) throws IOException {
        String[] parts = request.split(" ");

        if (parts.length != 3) {
            return;
        }

        String path = parts[1];

        if (!validPaths.contains(path)) {
            sendNotFound(out);
            return;
        }

        Path filePath = Path.of(".", "/public", path);
        String mimeType = Files.probeContentType(filePath);
        byte[] content;

        if (path.equals("/classic.html")) {
            String classicContent = Files.readString(filePath);
            content = classicContent
                    .replace("{time}", TimeUtils.getCurrentTime())
                    .getBytes();
        } else {
            content = Files.readAllBytes(filePath);
        }

        sendOK(out, content, mimeType);
    }

    private void sendNotFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void sendOK(BufferedOutputStream out, byte[] content, String mimeType) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }
}
