package com.lezhin.avengers.panther.notification;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author seoeun
 * @since 2017.11.24
 */
@Data
@Builder
public class SlackEvent {

    private String header;
    private long timestamp;
    private String title;
    private String message;
    private SlackMessage.LEVEL level;
    private Map<String, String> details;
    private Throwable exception;

}
