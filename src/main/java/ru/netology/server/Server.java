package ru.netology.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final static int PORT = 9999;
    private final List<String> validPaths = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
            "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public void startServer() {
        ExecutorService executor = Executors.newFixedThreadPool(64);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                executor.submit(new Processor(socket, validPaths));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
