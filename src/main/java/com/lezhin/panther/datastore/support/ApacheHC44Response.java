package com.lezhin.panther.datastore.support;

import com.google.api.client.http.LowLevelHttpResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author geunwoo.shin auth-server
 */
class ApacheHC44Response extends LowLevelHttpResponse {
    private final HttpRequestBase request;

    private final Header[] allHeaders;
    private final StatusLine statusLine;
    private final HttpEntity entity;

    ApacheHC44Response(final HttpRequestBase request, final HttpResponse response) {
        this.request = request;

        this.statusLine = response.getStatusLine();
        this.entity = response.getEntity();
        this.allHeaders = response.getAllHeaders();
    }

    @Override
    public InputStream getContent() throws IOException {
        return entity == null ? null : entity.getContent();
    }

    @Override
    public String getContentEncoding() throws IOException {
        if (entity != null) {
            Header contentEncodingHeader = entity.getContentEncoding();
            if (contentEncodingHeader != null) {
                return contentEncodingHeader.getValue();
            }
        }
        return null;
    }

    @Override
    public long getContentLength() throws IOException {
        return entity == null ? -1 : entity.getContentLength();
    }

    @Override
    public String getContentType() throws IOException {
        if (entity != null) {
            Header contentTypeHeader = entity.getContentType();
            if (contentTypeHeader != null) {
                return contentTypeHeader.getValue();
            }
        }
        return null;
    }

    @Override
    public String getStatusLine() throws IOException {
        return statusLine == null ? null : statusLine.toString();
    }

    @Override
    public int getStatusCode() throws IOException {
        return statusLine == null ? 0 : statusLine.getStatusCode();
    }

    @Override
    public String getReasonPhrase() throws IOException {
        return statusLine == null ? null : statusLine.getReasonPhrase();
    }

    @Override
    public int getHeaderCount() throws IOException {
        return allHeaders.length;
    }

    @Override
    public String getHeaderName(final int index) throws IOException {
        return allHeaders[index].getName();
    }

    @Override
    public String getHeaderValue(final int index) throws IOException {
        return allHeaders[index].getValue();
    }

    public void disconnect() throws IOException {
        request.abort();
    }
}
