package com.lezhin.panther.pg.pincrux;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author seoeun
 * @since 2018.03.20
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "adevent")
public class ADEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usrkey; // lezhin userId
    private String token; // lezhin authToken // TODO need?
    private Integer appkey;
    private Integer osFlag;
    private String appName; // attp 할 때, item의 appName
    private String appTitle; // postback 할 때, request param의 app_title. appName과 같기를 바라지만 다를 가능 성도 있음.
    private Integer cointInt; // attp 할 때, item의 coinInt
    private Integer coin; // postback 에서 받는 param의 coin. coinInt와 같아야 하지만 핀크럭스에서 수정할 경우 다를 수 있음.
    private String customUrl;
    private String transid;
    private Timestamp attpAt; // attp time
    private Timestamp postbackAt; // postback time
    private Timestamp rewardAt; // coin reward time (lezhin wallet)
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        attp, postback, reward
    }

}
