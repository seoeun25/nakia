package com.lezhin.support;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;

import java.io.IOException;

/**
 * @author geunwoo.shin auth-server
 */
public class ApacheHC44Transport extends HttpTransport {
    private final HttpClient client;

    public ApacheHC44Transport(final HttpClient client) {
        this.client = client;
    }

    @Override
    protected LowLevelHttpRequest buildRequest(final String method, final String url) throws IOException {
        HttpRequestBase request;
        if (method.equals(HttpMethods.DELETE)) {
            request = new HttpDelete(url);
        } else if (method.equals(HttpMethods.GET)) {
            request = new HttpGet(url);
        } else if (method.equals(HttpMethods.HEAD)) {
            request = new HttpHead(url);
        } else if (method.equals(HttpMethods.POST)) {
            request = new HttpPost(url);
        } else if (method.equals(HttpMethods.PUT)) {
            request = new HttpPut(url);
        } else if (method.equals(HttpMethods.TRACE)) {
            request = new HttpTrace(url);
        } else if (method.equals(HttpMethods.OPTIONS)) {
            request = new HttpOptions(url);
        } else {
            throw new UnsupportedOperationException(method + " " + url + " does not supported");
        }
        return new ApacheHC44Request(client, request);
    }
}
