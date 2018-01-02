package com.lezhin.config;

import com.lezhin.support.DatastoreRequestRetryHandler;

import com.google.api.client.googleapis.GoogleUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.IdleConnectionEvictor;
import org.apache.http.impl.conn.DefaultHttpClientConnectionOperator;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.InMemoryDnsResolver;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

public class DatastoreHttpClientConfiguration {
    private static final int CONNECTION_MAX_TOTAL = 400;
    private static final int MAX_PER_ROUTE = 100;

    private static final long CONNECTION_TTL = 60;
    private static final long IDLE_CONN_CHECK_INTERVAL = 1;
    private static final long MAX_CONN_IDLE_TIME = 30;

    private static final SystemDefaultDnsResolver DNS_RESOLVER = SystemDefaultDnsResolver.INSTANCE;
    private static final InMemoryDnsResolver CACHE = new InMemoryDnsResolver();

    private int maxRetryCount = 3;
    private int socketTimeout = 1800;
    private int connectTimeout = 1200;


    public HttpClient datastoreHttpClient() throws IOException, GeneralSecurityException {
        SSLContext defaultContext = SSLContexts.custom()
                .loadTrustMaterial(GoogleUtils.getCertificateTrustStore(), TrustSelfSignedStrategy.INSTANCE)
                .build();

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                defaultContext,
                NoopHostnameVerifier.INSTANCE
        );

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();

        DefaultHttpClientConnectionOperator operator = new DefaultHttpClientConnectionOperator(
                registry, DefaultSchemePortResolver.INSTANCE, host -> {
            try {
                InetAddress[] resolved = DNS_RESOLVER.resolve(host);
                CACHE.add(host, resolved);
                return resolved;
            } catch (UnknownHostException e) {
                return CACHE.resolve(host);
            }
        }
        );
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                operator, ManagedHttpClientConnectionFactory.INSTANCE, CONNECTION_TTL, TimeUnit.SECONDS
        );
        connectionManager.setValidateAfterInactivity(2000);
        connectionManager.setMaxTotal(CONNECTION_MAX_TOTAL);
        connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);

        new IdleConnectionEvictor(connectionManager,
                IDLE_CONN_CHECK_INTERVAL, TimeUnit.SECONDS,
                MAX_CONN_IDLE_TIME, TimeUnit.SECONDS).start();

        RequestConfig config = RequestConfig.custom()
                .setRedirectsEnabled(false)
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .build();

        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(config)
                .setRetryHandler(new DatastoreRequestRetryHandler(maxRetryCount))
                .build();
    }
}
