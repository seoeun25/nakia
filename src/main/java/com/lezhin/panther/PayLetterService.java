package com.lezhin.panther;

import com.lezhin.panther.Repository.PayLetterPaymentRepository;
import com.lezhin.panther.model.PayLetterLog;

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
