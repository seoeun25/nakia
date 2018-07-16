package com.lezhin.panther.pg.tapjoy;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author taemmy
 * @since 2018. 7. 2.
 */
@Repository
public interface TapjoyEventRepository extends CrudRepository<TapjoyEvent, Long> {
    List<TapjoyEvent> findBySnuidAndRequestId(final Long snuid, final String requestId);
}
