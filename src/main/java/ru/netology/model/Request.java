package ru.netology.model;

public class Request {

    private String method;
    private String uri;
    private String headers;
    private String body;

    public Request(String method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
