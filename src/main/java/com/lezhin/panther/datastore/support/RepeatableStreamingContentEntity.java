package com.lezhin.panther.datastore.support;

import com.google.api.client.util.StreamingContent;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author geunwoo.shin auth-server
 */
class RepeatableStreamingContentEntity extends AbstractHttpEntity {
    private final StreamingContent streamingContent;
    private final long contentLength;
    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

    RepeatableStreamingContentEntity(final String contentType, final String contentEncoding,
                                     final StreamingContent streamingContent, final long contentLength) {
        this.setContentType(contentType);
        this.setContentEncoding(contentEncoding);
        this.streamingContent = streamingContent;
        this.contentLength = contentLength;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        if (contentLength != 0) {
            if (buf.size() == 0) {
                streamingContent.writeTo(buf);
            }
            outstream.write(buf.toByteArray());
        }
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    void flushBuffer() {
        try {
            buf.flush();
        } catch (IOException e) {
        }
    }
}
