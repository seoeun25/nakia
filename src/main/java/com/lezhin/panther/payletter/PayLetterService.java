package com.lezhin.panther.payletter;

import com.lezhin.panther.datastore.PayLetterPaymentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * @author benjamin
 * @since 2017.12.19
 */
@Service
public class PayLetterService {

    private static final Logger logger = LoggerFactory.getLogger(PayLetterService.class);
    private PayLetterPaymentRepository paymentDataStoreRepository;

    public PayLetterService(final PayLetterPaymentRepository paymentDataStoreRepository) {
        this.paymentDataStoreRepository = paymentDataStoreRepository;
    }

    public List<PayLetterLog> getLogs(Instant startDateTime, Instant endDateTime, String locale) {
        List<PayLetterLog> logs = paymentDataStoreRepository.getLogs(startDateTime.toEpochMilli(), endDateTime.toEpochMilli(), locale);

        return logs;
    }

}
