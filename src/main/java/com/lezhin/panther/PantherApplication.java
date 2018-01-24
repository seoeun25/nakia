package com.lezhin.panther;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Panther Application Main.
 *
 * @author seoeun
 * @since 2017.10.24
 */
@SpringBootApplication
@EnableScheduling
public class PantherApplication {

    public static final String APP_NAME = "panther";

    public static void main(String[] args) {
        SpringApplication.run(PantherApplication.class, args);
    }
}
