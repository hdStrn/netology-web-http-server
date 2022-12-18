package ru.netology.model;

import org.apache.http.NameValuePair;

import java.util.List;

public class Request {

    private String method;
    private String path;
    private List<NameValuePair> queryParams;
    private List<String> headers;
    private String body;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<NameValuePair> queryParams() {
        return queryParams;
    }

    public void setQueryParams(List<NameValuePair> queryParams) {
        this.queryParams = queryParams;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getQueryParam(String name) {
        for (NameValuePair queryParam : queryParams()) {
            if (name.equals(queryParam.getName())) {
                return queryParam.getValue();
            }
        }
        System.out.println("There is no such query parameter");
        return null;
    }

    @Override
    public String toString() {
        return "Request {\n" +
                "\tmethod='" + method + "',\n" +
                "\tpath='" + path + "',\n" +
                "\tqueryParams=" + queryParams + ",\n" +
                "\theaders=" + headers + ",\n" +
                "\tbody='" + body + "',\n" +
                '}';
    }
}
