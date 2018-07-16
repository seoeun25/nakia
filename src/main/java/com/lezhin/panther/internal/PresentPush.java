package com.lezhin.panther.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lezhin.panther.util.DateUtil;
import lombok.Data;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Present Push RequestBody
 * @author taemmy
 * @since 2018. 7. 3.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PresentPush {

    private String type;
    private List<Recipient> recipients;
    private DefaultMessage default_message;
    private Schedule schedule;

    public PresentPush(Long user_id, String custom_uri, String title, String body ) {
        this.type  = "normal";
        this.recipients = new ArrayList<>();
        recipients.add(new Recipient(user_id));
        this.default_message = new DefaultMessage(title, body, custom_uri);
        this.schedule = new Schedule();
        String formattedDate = DateUtil.format(DateTime.now().getMillis(),DateUtil.ASIA_SEOUL_ZONE, DateUtil.DATE_TIME_FORMATTER );
        this.schedule.setTime(formattedDate);
    }

    @Data
    public class Recipient{
        private Long user_id;
        private String variant;

        public Recipient(Long user_id) {
            this.variant = "A";
            this.user_id = user_id;
        }
    }

    @Data
    public class DefaultMessage {
        private Integer notification_id;
        private String custom_uri;
        private String title;
        private String body;

        public DefaultMessage(String title, String body, String custom_uri) {
            this.notification_id = 1;
            this.custom_uri = custom_uri;
            this.title = title;
            this.body = body;
        }
    }

    @Data
    public class Schedule {
        private Boolean in_local_time = true;
        private Boolean at_optimal_time = false;
        private String time;
    }
}
