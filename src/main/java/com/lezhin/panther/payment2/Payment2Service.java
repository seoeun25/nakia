package com.lezhin.panther.payment2;

import com.lezhin.panther.datastore.LezhinDataStore;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Datastore의 Payment2 에 접근하여 작업.
 *
 * @author seoeun
 * @since 2018.02.21
 */
@Service
public class Payment2Service {

    private static final Logger logger = LoggerFactory.getLogger(Payment2Service.class);

    private static final String PAYMENT2_KIND = "Payment2";
    private LezhinDataStore lezhinDataStore;

    public Payment2Service(final LezhinDataStore lezhinDataStore) {
        this.lezhinDataStore = lezhinDataStore;
    }

    public List<Entity> get(Long userId, Long startCreatedAt, Long endCreatedAt) {
        Key userKey = lezhinDataStore.getDatastore().newKeyFactory().kind("UserAccount").newKey(userId);
        StructuredQuery<Entity> query =
                Query.entityQueryBuilder()
                        .kind(PAYMENT2_KIND)
                        .filter(StructuredQuery.CompositeFilter.and(
                                StructuredQuery.PropertyFilter.hasAncestor(userKey),
                                StructuredQuery.PropertyFilter.ge("createdAt", startCreatedAt),
                                StructuredQuery.PropertyFilter.le("createdAt", endCreatedAt)))
                        .build();

        QueryResults<Entity> results = lezhinDataStore.Query(query);
        List<Entity> entities = new ArrayList<>();
        int count = 0;
        while (results.hasNext()) {
            Entity result = results.next();
            logger.info("" + result.key());
            logger.info("idUser = {}, paymentId = {}, createdAt={}, {}, {}",
                    result.getLong("idUser"), result.key().id(), result.getLong("createdAt"),
                    result.getString("state"), result.getString("paymentType"));
            entities.add(result);
            count++;
        }

        logger.info("select = {}", count);
        return entities;

    }

    public void updateState(Long userId, Long startCreatedAt, Long endCreatedAt, String state) {

        logger.info("updateState. userId={}, startCreatedAt={}, endCreatedAt={}, state={}",
                userId, startCreatedAt, endCreatedAt, state);
        List<Entity> results = get(userId, startCreatedAt, endCreatedAt);
        int count = 0;
        for (Entity e : results) {
            Entity newEntity = Entity.builder(e).set("state", state).build();
            lezhinDataStore.update(newEntity);
            count++;
        }

        logger.info("updated = {}", count);
    }

    public List<Entity> getPayment(Long userId, Long paymentId) {
        logger.info("request.userId={}, paymentId = {}", userId, paymentId);
        PathElement userAccountPath = PathElement.of("UserAccount", userId);

        Key paymentKey = lezhinDataStore.getDatastore().newKeyFactory()
                .kind("Payment2").ancestors(userAccountPath).newKey(paymentId);
        logger.info("paymentKey = {}", paymentKey);
        StructuredQuery<Entity> query =
                Query.entityQueryBuilder()
                        .kind(PAYMENT2_KIND)
                        .filter(StructuredQuery.CompositeFilter.and(
                                StructuredQuery.PropertyFilter.hasAncestor(paymentKey))
                                )
                        .build();

        QueryResults<Entity> results = lezhinDataStore.Query(query);
        List<Entity> entities = new ArrayList<>();
        int count = 0;
        while (results.hasNext()) {
            Entity result = results.next();
            logger.info("idUser = {}, paymentId = {}, createdAt={}, {}, {}",
                    result.getLong("idUser"), result.key().id(), result.getLong("createdAt"),
                    result.getString("state"), result.getString("paymentType"));
            entities.add(result);
            count++;
        }

        logger.info("select = {}", count);
        return entities;

    }

}
