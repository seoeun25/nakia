package com.lezhin.avengers.panther;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Panther Application Main.
 *
 * @author seoeun
 * @since 2017.10.24
 */
@SpringBootApplication
public class PantherApplication {

    private static final Logger logger = LoggerFactory.getLogger(PantherApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(PantherApplication.class, args);
    }
}
