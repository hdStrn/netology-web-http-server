package ru.netology.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final int port = 9999;
    private final List<String> allowedMethods = List.of("GET", "POST");
    private Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>(
            Map.of(allowedMethods.get(0), new ConcurrentHashMap<>(),
                    allowedMethods.get(1), new ConcurrentHashMap<>()));

    public void listen() {
        ExecutorService executor = Executors.newFixedThreadPool(64);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                executor.submit(new Processor(socket, handlers));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!allowedMethods.contains(method)) {
            System.out.println("Not allowed method!");
        }
        handlers.get(method).put(path, handler);
        System.out.printf("Handler for '%s %s' added!\n", method, path);
    }


}
