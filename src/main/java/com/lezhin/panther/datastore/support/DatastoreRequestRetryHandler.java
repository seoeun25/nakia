package com.lezhin.panther.datastore.support;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author geunwoo.shin auth-server
 */
public class DatastoreRequestRetryHandler implements HttpRequestRetryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatastoreRequestRetryHandler.class);

    private static final Collection<Class<? extends Exception>> RETRY_ALLOWED_EXCEPTIONS = Arrays.asList(
            ConnectionPoolTimeoutException.class,
            SocketTimeoutException.class,
            GoogleJsonResponseException.class
    );

    private final int maxRetryCount;

    public DatastoreRequestRetryHandler(final int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    @Override
    public boolean retryRequest(final IOException exception, final int executionCount, final HttpContext context) {
        if (executionCount > maxRetryCount) {
            LOGGER.error(exception.getLocalizedMessage(), exception);
            return false;
        }

        if (RETRY_ALLOWED_EXCEPTIONS.contains(exception.getClass())) {
            LOGGER.warn(exception.getLocalizedMessage(), exception);
            return true;
        } else {
            LOGGER.error(exception.getLocalizedMessage(), exception);
            return false;
        }
    }
}
