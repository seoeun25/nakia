package com.lezhin.avengers.panther;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author seoeun
 * @since 2017.11.10
 */
@Service
@Slf4j
public class CacheService {

    public CacheService() {

    }

    public String[] getMemberInfo(Long userId) {

        String name = "";
        String CI = "";

        return new String[]{name, CI};
    }


}
