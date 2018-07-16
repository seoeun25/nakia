package com.lezhin.panther.pg.pincrux;


import com.lezhin.panther.util.DateUtil;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 핀크럭스 광고 요청 데이터
 *
 * @author benjamin
 * @since 2017.1.12
 */
@Deprecated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PinCruxPushRequest implements Serializable {

    private String type = "normal";
    private List<Recipient> recipients = new ArrayList<>();
    private DefaultMessage default_message;
    private Schedule schedule = new Schedule();


    @Data
    public class Recipient{
        Long user_id;
        String variant = "A";
        public Recipient(Long user_id){this.user_id = user_id;}
    }

    @Data
    public class DefaultMessage{
        Integer notification_id = 1;
        String custom_uri;
        String title;
        String body;
        public DefaultMessage(String title, String body, String custom_uri){
            this.title = title;
            this.body = body;
            this.custom_uri = custom_uri;
        }
    }

    @Data
    public class Schedule{
        Boolean in_local_time = true;
        Boolean at_optimal_time = false;
        String time;
    }

    public PinCruxPushRequest(Long user_id,  String custom_uri, String title, String body ){
        this.recipients.add(new Recipient(user_id));
        this.default_message = new DefaultMessage( title, body, custom_uri);
        String formattedDate = DateUtil.format(DateTime.now().getMillis(),DateUtil.ASIA_SEOUL_ZONE, DateUtil.DATE_TIME_FORMATTER );
        this.schedule.setTime(formattedDate);
    }

}


