package ru.netology.server;

import ru.netology.model.Request;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

public class Processor implements Runnable {

    private Socket socket;
    private Map<String, Map<String, Handler>> handlers;

    public Processor(Socket socket, Map<String, Map<String, Handler>> handlers) {
        this.socket = socket;
        this.handlers = handlers;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            String req = in.readLine();
            processRequest(req, out);
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

    private void processRequest(String req, BufferedOutputStream out) throws IOException {
        String[] parts = req.split(" ");

        if (parts.length != 3) {
            return;
        }

        Request request = new Request(parts[0], parts[1]);
        Handler handler = handlers.get(request.getMethod()).get(request.getUri());

        if (handler == null) {
            sendNotFound(out);
        } else {
            handler.handle(request, out);
        }
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
}
