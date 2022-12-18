package ru.netology.server;

import ru.netology.model.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;

@FunctionalInterface
public interface Handler {

    void handle(Request request, BufferedOutputStream out) throws IOException;
}
