package com.lezhin.panther.pg.pincrux;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
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
    private Integer osflag;
    private String appname; // attp 할 때, item의 appName
    private String apptitle; // postback 할 때, request param의 app_title. appName과 같기를 바라지만 다를 가능 성도 있음.
    private Integer coinint; // attp 할 때, item의 coinInt
    private Integer coin; // postback 에서 받는 param의 coin. coinInt와 같아야 하지만 핀크럭스에서 수정할 경우 다를 수 있음.
    private String customurl;
    @Column(name = "trans_id")
    private String transId;
    @Column(name = "attp_at")
    private Timestamp attpAt; // attp time
    @Column(name = "postback_at")
    private Timestamp postbackAt; // postback time
    @Column(name = "reward_at")
    private Timestamp rewardAt; // coin reward time (lezhin wallet)
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        attp, postback, reward
    }

}
