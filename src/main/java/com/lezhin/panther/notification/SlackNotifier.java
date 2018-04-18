package com.lezhin.panther.notification;

import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.util.DateUtil;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.11.24
 */
@Service
@Qualifier("slackNotifier")
public class SlackNotifier {

    public static final String LEZHIN_SLACK_URL =
            "https://hooks.slack.com/services/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";
    public static final String DONDOG_SLACK_URL =
            "https://hooks.slack.com/services/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";
    private static final Logger logger = LoggerFactory.getLogger(SlackNotifier.class);
    private String channel;

    private PantherProperties pantherProperties;

    private ClientHttpRequestFactory clientHttpRequestFactory;

    public SlackNotifier(final PantherProperties pantherProperties,
                         final ClientHttpRequestFactory clientHttpRequestFactory) {
        this.pantherProperties = pantherProperties;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.channel = "#" + pantherProperties.getSlackChannel();
    }

    public static String stackTrace(final Throwable e, final int size) {
        if (e == null) {
            return "";
        }
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        int max = size;
        StringBuilder builder = new StringBuilder(e.toString() + "\n");
        for (int i = 0; i < max; i++) {
            builder.append("\tat " + stackTraceElements[i] + "\n");
        }

        Throwable cause = e.getCause();
        if (cause != null) {
            builder.append(cause.getMessage());
            StackTraceElement[] stackTraceElements2 = cause.getStackTrace();
            for (int i = 0; i < max; i++) {
                builder.append("\tat " + stackTraceElements2[i] + "\n");
            }
        }
        return builder.toString();
    }

    public void notify(final SlackEvent event) {
        SlackMessage slackMessage = null;
        try {
            if (event.getTimestamp() == 0) {
                event.setTimestamp(Instant.now().toEpochMilli());
            }
            slackMessage = create(event);
            int response = send(slackMessage);
            if (response != 200) {
                throw new Exception("Slack Hook response = " + response);
            }
        } catch (Exception e) {
            logger.warn("Failed to send to slack: channel={}, slackMessage={}",
                    channel, slackMessage);
        }
    }

    public void notify(Throwable e) {
        Executor.Type type = Executor.Type.UNKNOWN;
        SlackMessage.LEVEL slackLevel = levelOf(e);
        String title = e.getMessage();
        String message = "";
        if (e instanceof PantherException) {
            PantherException pantherException = (PantherException) e;
            type = pantherException.getExecutorType().orElse(Executor.Type.UNKNOWN);
        } else {
            title = "Unexpected error";
            message = e.getMessage();
        }
        String header = type == Executor.Type.UNKNOWN ? "" : type.name();
        if (slackLevel != null) {
            notify(SlackEvent.builder()
                    .header(header)
                    .level(slackLevel)
                    .title(title)
                    .message(message)
                    .exception(e)
                    .build());
        }
    }

    public SlackMessage.LEVEL levelOf(Throwable e) {
        SlackMessage.LEVEL level = null;
        if (e instanceof PantherException) {
            PantherException pantherException = (PantherException) e;
            Class pantherExceptionClass = pantherException.getClass();
            if (pantherExceptionClass.isAnnotationPresent(NotificationLevel.class)) {
                Annotation annotation = pantherExceptionClass.getAnnotation(NotificationLevel.class);
                NotificationLevel notificationLevel = (NotificationLevel) annotation;
                if (notificationLevel.level() == NotificationLevel.Level.INFO) {
                    level = SlackMessage.LEVEL.INFO;
                } else if (notificationLevel.level() == NotificationLevel.Level.WARN) {
                    level = SlackMessage.LEVEL.WARN;
                } else if (notificationLevel.level() == NotificationLevel.Level.ERROR) {
                    level = SlackMessage.LEVEL.ERROR;
                } else { // NONE 포함.
                    level = null;
                }
            }
        } else {
            level = SlackMessage.LEVEL.ERROR;
        }
        return level;
    }

    private SlackMessage create(SlackEvent event) {
        String pretext = String.format("*[%s]  %s*",
                DateUtil.format(event.getTimestamp(), DateUtil.ASIA_SEOUL_ZONE, "yyyy-MM-dd HH:mm:ss"),
                event.getHeader()
        );

        StringBuilder details = new StringBuilder();
        if (event.getDetails() != null) {
            event.getDetails().entrySet().stream().forEach(
                    e -> details.append("    " + e.getKey() + " = " + e.getValue() + "\n"));
        }
        String exceptionTrace = stackTrace(event.getException(), 4);
        String text = String.format("%s\n%s\n```%s```", event.getMessage(), details, exceptionTrace);

        SlackMessage slackAttachments = SlackMessage.builder()
                .attachment(SlackMessage.Attachment.builder().fallback("fallback")
                        .pretext(pretext)
                        .author_name("Panther")
                        .author_link(pantherProperties.getCloudWatch())
                        .title(event.getTitle())
                        .text(text)
                        .color(event.getLevel().getColor())
                        .field(SlackMessage.Field.builder()
                                .title("") // event.getLevel().name()
                                .build())
                        .mrkdwn_in(ImmutableList.of("pretext", "text"))
                        .build())
                .channel(channel)
                .username(SlackMessage.USERNAME)
                .icon_emoji(SlackMessage.ICON_EMOJI)
                .build();
        return slackAttachments;
    }

    protected int send(SlackMessage slackMessage) throws Exception {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(LEZHIN_SLACK_URL, slackMessage, String.class);
        } catch (Exception e) {
            logger.warn("Failed to send to slack" + e.getMessage());
        }

        return Optional.ofNullable(response).map((ResponseEntity r) -> r.getStatusCode().value())
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

}
