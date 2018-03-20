package com.lezhin.panther.datastore;

import com.lezhin.panther.config.AppEngineProperties;
import com.lezhin.panther.datastore.support.ApacheHC44Transport;

import com.google.api.client.util.PemReader;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.StringUtils;
import com.google.cloud.AuthCredentials;
import com.google.cloud.RetryParams;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * @author benjamin
 * @date 2017-12-21
 */
@Component
public class LezhinDataStore {
    private static final Logger logger = LoggerFactory.getLogger(LezhinDataStore.class);
    private AppEngineProperties appEngineProperties;
    private Datastore datastore;
    private HttpClient httpClient;

    public LezhinDataStore(final AppEngineProperties appEngineProperties) {
        this.appEngineProperties = appEngineProperties;
        this.httpClient = createHttpClient();
        try {
            this.datastore = createDataStoreClient();
        } catch (Exception e) {
            logger.error("PaymentDataStoreRepository create error {}", e);
        }
    }

    // TODO rename. use lowercase
    public QueryResults<Entity> Query(Query<Entity> query) {
        return datastore.run(query);
    }

    public void update(Entity entity) {
        datastore.update(entity);
    }

    public Datastore getDatastore() {
        return datastore;
    }


    private HttpClient createHttpClient() {
        try {
            return new DatastoreHttpClientConfiguration().datastoreHttpClient();
        } catch (Exception e) {
            return HttpClientBuilder.create().build();
        }
    }

    private Datastore createDataStoreClient() throws Exception {
        String privateKey = StringUtils.newStringUtf8(Base64.getDecoder().decode(appEngineProperties.getPrivateKey()));
        Reader reader = new StringReader(privateKey);
        PemReader.Section section = PemReader.readFirstSectionAndClose(reader, "PRIVATE KEY");
        if (section == null) {
            throw new IOException("Invalid PKCS8 data.");
        }
        byte[] bytes = section.getBase64DecodedBytes();
        PrivateKey pkey = SecurityUtils.getRsaKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(bytes));
        return buildDatastore(
                appEngineProperties.getServiceAccountId(),
                pkey,
                appEngineProperties.getDatasetId(),
                httpClient
        );
    }

    private Datastore buildDatastore(
            final String account, final PrivateKey privateKey, final String projectId,
            final HttpClient client) {

        RetryParams retryParams = RetryParams.builder()
                .retryMinAttempts(2)
                .retryMaxAttempts(3)
                .initialRetryDelayMillis(50)
                .maxRetryDelayMillis(300)
                .build();

        DatastoreOptions options = DatastoreOptions.builder()
                .projectId(projectId)
                .authCredentials(AuthCredentials.createFor(account, privateKey))
                .httpTransportFactory(() -> new ApacheHC44Transport(client))
                .retryParams(retryParams)
                .build();

        return options.service();
    }


}
