package com.lezhin.avengers.panther.notification;

import com.lezhin.avengers.panther.config.LezhinProperties;
import com.lezhin.avengers.panther.util.DateUtil;
import com.lezhin.avengers.panther.util.JsonUtil;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.11.24
 */
@Service
public class SlackNotifier {

    public static final String LEZHIN_SLACK_URL = "https://hooks.slack.com/services/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";
    private static final Logger logger = LoggerFactory.getLogger(SlackNotifier.class);
    private String channel;

    private LezhinProperties lezhinProperties;

    private ClientHttpRequestFactory clientHttpRequestFactory;

    public SlackNotifier(final LezhinProperties lezhinProperties,
                         final ClientHttpRequestFactory clientHttpRequestFactory) {
        this.lezhinProperties = lezhinProperties;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.channel = "#" + lezhinProperties.getSlackChannel();
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
                    channel, slackMessage, e);
        }
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
                        .author_link(lezhinProperties.getCloudWatch())
                        .title(event.getTitle())
                        .text(text)
                        .color(event.getLevel().getColor())
                        .field(SlackMessage.Field.builder()
                                .title(event.getLevel().name())
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
            logger.warn("Failed to send to slack", e);
        }

        return Optional.ofNullable(response).map((ResponseEntity r) -> r.getStatusCode().value())
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

}
