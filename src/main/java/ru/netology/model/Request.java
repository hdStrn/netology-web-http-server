package ru.netology.model;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Map;

public class Request {

    private String method;
    private String path;
    private List<NameValuePair> queryParams;
    private Map<String, List<String>> postParams;
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

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<NameValuePair> queryParams) {
        this.queryParams = queryParams;
    }

    public Map<String, List<String>> getPostParams() {
        return postParams;
    }

    public void setPostParams(Map<String, List<String>> postParams) {
        this.postParams = postParams;
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
        for (NameValuePair queryParam : getQueryParams()) {
            if (name.equals(queryParam.getName())) {
                return queryParam.getValue();
            }
        }
        System.out.println("There is no such query parameter");
        return null;
    }

    public List<String> getPostParam(String name) {
        if (postParams.containsKey(name)) {
            return postParams.get(name);
        }
        System.out.println("There is no such post parameter");
        return null;
    }


    @Override
    public String toString() {
        return "Request {\n" +
                "\tmethod='" + method + "',\n" +
                "\tpath='" + path + "',\n" +
                "\tqueryParams=" + queryParams + ",\n" +
                "\tpostParams=" + postParams + ",\n" +
                "\theaders=" + headers + ",\n" +
                "\tbody='" + body + "',\n" +
                '}';
    }
}
