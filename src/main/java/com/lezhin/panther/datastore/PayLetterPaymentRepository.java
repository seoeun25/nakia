package com.lezhin.panther.datastore;

import com.lezhin.panther.payletter.PayLetterLog;
import com.lezhin.panther.util.DateUtil;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/**
 * @author benjamin
 * @date 2017-12-21
 */

@Repository
public class PayLetterPaymentRepository {
    private static final Logger logger = LoggerFactory.getLogger(PayLetterPaymentRepository.class);
    private final String KIND = "Payment2";
    private LezhinDataStore lezhinDataStore;

    public PayLetterPaymentRepository(final LezhinDataStore lezhinDataStore) {
        this.lezhinDataStore = lezhinDataStore;
    }

    public List<PayLetterLog> getLogs(Long startDate, Long endDate, String locale) {
        logger.info("PaymentDataStoreRepository startDate {} endDate {}", startDate, endDate);
//        String gqlQuery = String.format("select * from %s where ");
//        Query<Entity> query = Query.gqlQueryBuilder(Query.ResultType.ENTITY, gqlQuery).build();


        Query<Entity> query = Query.entityQueryBuilder()
                .kind(KIND)
                .filter(StructuredQuery.CompositeFilter.and(
                        PropertyFilter.eq("paymentType", "payletter"),
                        PropertyFilter.ge("createdAt", startDate),
                        PropertyFilter.le("createdAt", endDate)))
                .build();
        QueryResults<Entity> results = this.lezhinDataStore.Query(query);
        Query<Entity> queryM = Query.entityQueryBuilder()
                .kind(KIND)
                .filter(StructuredQuery.CompositeFilter.and(
                        PropertyFilter.eq("paymentType", "mpayletter"),
                        PropertyFilter.ge("createdAt", startDate),
                        PropertyFilter.le("createdAt", endDate)))
                .build();
        QueryResults<Entity> resultsM = this.lezhinDataStore.Query(queryM);

        List<PayLetterLog> logs = new ArrayList<>();
        while (results.hasNext() || resultsM.hasNext()) {
            Entity currentEntity = results.hasNext() ? results.next() : resultsM.next();
            String localeData = currentEntity.getKey("locale").name();
            String state = currentEntity.getString("state");
            if ((StringUtil.isNullOrEmpty(locale) || locale.toLowerCase().equals(localeData.toLowerCase()))
                    && !state.equals("R")
                    ) {/*locale 필터링은 거의 사용하지 않아 굳이 인덱스를 추가할 필요가 없어 루프에서 처리.*/
                PayLetterLog log = new PayLetterLog();
                log.setCurrency(currentEntity.getString("currency"));
                log.setLezhinPaymentId(currentEntity.key().id());
                log.setUserId(currentEntity.getLong("idUser"));
                log.setPaidAmount((float) currentEntity.getDouble("amount"));
                log.setState(currentEntity.getString("state"));
                Date date = new Date(currentEntity.getLong("createdAt"));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                SimpleDateFormat ymd = new SimpleDateFormat("yyyyMMdd");
                sdf.setTimeZone(TimeZone.getTimeZone(DateUtil.ASIA_SEOUL_ZONE));
                ymd.setTimeZone(TimeZone.getTimeZone(DateUtil.ASIA_SEOUL_ZONE));
                String formattedDate = DateUtil.format(date.getTime(), DateUtil.ASIA_SEOUL_ZONE, DateUtil.DATE_TIME_FORMATTER);
                String formattedDate2 = DateUtil.format(date.getTime(), DateUtil.ASIA_SEOUL_ZONE, DateUtil.DATE_FORMATTER);
                log.setRegDate(formattedDate);
                log.setYmd(formattedDate2);
                log.setPaymentType(currentEntity.getString("paymentType"));
                log.setLocale(localeData);
                log.setIdApproval(currentEntity.getString("idApproval"));
                log.setPgCompany(currentEntity.getString("pgCompany"));
                log.setRefIdPayment(currentEntity.isNull("refIdPayment") ? null : currentEntity.getLong("refIdPayment"));

                logs.add(log);
            }
        }
        // descending by paymentId
        Collections.sort(logs, (o1, o2) -> o2.getLezhinPaymentId().compareTo(o1.getLezhinPaymentId()));
        return logs;
    }
}


