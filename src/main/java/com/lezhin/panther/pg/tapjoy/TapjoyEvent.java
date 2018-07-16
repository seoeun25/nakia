package com.lezhin.panther.pg.tapjoy;

import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.executor.Executor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * @author taemmy
 * @since 2018. 6. 29.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "tapjoyevent")
@Table(indexes = {@Index(name = "tapjoyevent_snuid", columnList = "snuid"),
        @Index(name = "tapjoyevent_request_id", columnList = "request_id")})
public class TapjoyEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snuid", nullable = false)
    private Long snuid;                 // lezhin userId

    @Column(name = "request_id", nullable = false)
    private String requestId;           // tapjoy id

    @Column(name = "currency", nullable = false)
    private Integer currency;           // 보상금액

    @Column(name = "display_multiplier")
    private Double displayMultiplier;   // @Deprecated 예정

    @Column(name = "verifier", nullable = false)
    private String verifier;            // 부정 사용방지용 verifier

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "platform", nullable = false, length = 10)
    private String platform;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "postback_at")
    private Timestamp postbackAt;

    @Column(name = "reward_at")
    private Timestamp rewardAt;

    public enum Status {
        postback, reward
    }
}
