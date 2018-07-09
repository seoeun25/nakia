package com.lezhin.panther.pg.pincrux;

import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author seoeun
 * @since 2018.04.04
 */
public interface ADEventRepository extends CrudRepository<ADEvent, Long> {

    List<ADEvent> findByUsrkeyAndAppkeyAndOsflagOrderByIdDesc(Long usrkey, Integer appkey, Integer osFlag);

    List<ADEvent> findByUsrkeyAndAppkeyAndOsflagAndTransIdOrderByIdDesc(Long usrkey, Integer appkey, Integer osFlag,
                                                                        String transid);

    List<ADEvent> findByAttpAtBetween(Timestamp start, Timestamp end);

    List<ADEvent> findByUsrkeyOrderByIdDesc(Long usrkey);

}
