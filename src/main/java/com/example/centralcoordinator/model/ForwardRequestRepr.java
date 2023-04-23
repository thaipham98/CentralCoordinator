package com.example.centralcoordinator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;

public class ForwardRequestRepr {
    @JsonProperty("method")
    String method;
    @JsonProperty("contentType")
    String contentType;
    @JsonProperty("url")
    String url;
    @JsonProperty("queryString")
    String queryString;
    @JsonProperty("pathInfo")
    String pathInfo;
    @JsonProperty("body")
    String body;

    public ForwardRequestRepr(){};

    public ForwardRequestRepr(HttpServletRequest forwardRequest, String forwardRequestBody) {
        // print values of the forward request
        // mapping from HttpServletRequest to ForwardRequestRepr
        this.method = forwardRequest.getMethod();
        this.contentType = forwardRequest.getContentType();
        this.url = forwardRequest.getRequestURL().toString();
        this.queryString = forwardRequest.getQueryString();
        this.pathInfo = forwardRequest.getPathInfo();
        this.body = forwardRequestBody;
    }

    public ForwardRequestRepr(HttpServletRequest forwardRequest) {
        // mapping from HttpServletRequest to ForwardRequestRepr
        this.method = forwardRequest.getMethod();
        this.contentType = forwardRequest.getContentType();
        this.url = forwardRequest.getRequestURL().toString();
        this.queryString = forwardRequest.getQueryString();
        this.pathInfo = forwardRequest.getPathInfo();
        this.body = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForwardRequestRepr that = (ForwardRequestRepr) o;
        return Objects.equals(method, that.method) && Objects.equals(url, that.url) && Objects.equals(queryString, that.queryString) && Objects.equals(pathInfo, that.pathInfo) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, contentType, url, queryString, pathInfo, body);
    }

    @Override
    public String toString() {
        return "ForwardRequestRepr{" +
                "method='" + method + '\'' +
                ", contentType='" + contentType + '\'' +
                ", url='" + url + '\'' +
                ", queryString='" + queryString + '\'' +
                ", pathInfo='" + pathInfo + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    public String getMethod() {
        return method;
    }

    public String getContentType() {
        return contentType;
    }

    public String getUrl() {
        return url;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getBody() {
        return body;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
