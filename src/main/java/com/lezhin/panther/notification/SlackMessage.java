package com.lezhin.panther.notification;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * https://api.slack.com/docs/messages
 * @author seoeun
 * @since 2017.11.25
 */
@Data
@Builder
public class SlackMessage {

    public static final String USERNAME = "panther-bot";
    public static final String ICON_EMOJI = ":star:";


    private String channel;
    private String username;
    private String icon_emoji;
    private String text;

    @Singular
    private List<Attachment> attachments;

    public enum LEVEL {

        INFO("#36a64f"),
        WARN("warning"),
        ERROR("#C6171E");

        private String color;

        LEVEL(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }

    @Data
    @Builder
    public static class Attachment {

        private String fallback;
        private String color;
        private String pretext;
        private String author_name;
        private String author_link;
        private String author_icon;
        private String title;
        private String title_link;
        private String text;
        @Singular
        private List<Field> fields;
        private String image_url;
        private String thumb_url;
        private String footer;
        private String footer_icon;
        private Long ts;
        @Singular("mrkdwn_in")
        private List<String> mrkdwn_in;
    }

    @Data
    @Builder
    public static class Field {
        private String title;
        private String value;
    }
}
