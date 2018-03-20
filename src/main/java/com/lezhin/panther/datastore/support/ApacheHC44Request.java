package com.lezhin.panther.datastore.support;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.StreamingContent;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;

/**
 * @author geunwoo.shin from auth-server
 */
class ApacheHC44Request extends LowLevelHttpRequest {
    private final HttpClient client;
    private final HttpRequestBase request;

    ApacheHC44Request(final HttpClient client, final HttpRequestBase request) {
        this.client = client;
        this.request = request;
    }

    @Override
    public void addHeader(final String name, final String value) throws IOException {
        this.request.addHeader(name, value);
    }

    @Override
    public LowLevelHttpResponse execute() throws IOException {
        final StreamingContent streamingContent = getStreamingContent();
        RepeatableStreamingContentEntity entity = null;
        if (streamingContent != null && request instanceof HttpEntityEnclosingRequest) {
            entity = new RepeatableStreamingContentEntity(
                    getContentType(), getContentEncoding(), streamingContent, getContentLength()
            );
            ((HttpEntityEnclosingRequest) request).setEntity(entity);
        }
        try {
            return new ApacheHC44Response(request, client.execute(request));
        } finally {
            if (entity != null) {
                entity.flushBuffer();
            }
        }
    }
}
