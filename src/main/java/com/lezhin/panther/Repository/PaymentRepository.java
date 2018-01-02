package com.lezhin.panther.Repository;


import java.util.List;

/**
 * @author benjamin
 * @date 2017-12-21
 */

interface PaymentRepository<L> {
    List<L> getLogs(Long startDate, Long endDate, String locale);
}
