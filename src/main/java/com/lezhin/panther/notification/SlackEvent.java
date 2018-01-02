package com.lezhin.panther.notification;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

/**
 * @author seoeun
 * @since 2017.11.24
 */
@Builder
@Getter
@ToString
public class SlackEvent {

    private String header;
    private long timestamp;
    private String title;
    private String message;
    private SlackMessage.LEVEL level;
    private Map<String, String> details;
    private Throwable exception;

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
