package com.lezhin.panther.notification;

import com.lezhin.panther.Context;
import com.lezhin.panther.exception.CIException;
import com.lezhin.panther.exception.ExceedException;
import com.lezhin.panther.exception.ExecutorException;
import com.lezhin.panther.exception.FraudException;
import com.lezhin.panther.exception.HappyPointParamException;
import com.lezhin.panther.exception.HappyPointSystemException;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.exception.LguDepositException;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.exception.SessionException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.util.JsonUtil;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


/**
 * 실제로 slack (Lezhin Entertainment #panther_beta)로 메시지가 전달되기 때문에 @Test 를 막아놓음. 필요시 풀어서 테스트.
 *
 * @author seoeun
 * @since 2017.11.25
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class SlackNotifierTest {

    private static final Logger logger = LoggerFactory.getLogger(SlackNotifierTest.class);

    @Autowired
    private SlackNotifier slackNotifier;

    @Mock
    private Context mockContext;
    @Mock
    private RequestInfo mockRequestInfo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    //@Test
    public void test() {
        SlackMessage slackAttachments = SlackMessage.builder()
                .attachment(SlackMessage.Attachment.builder().fallback("fallback")
                        .pretext("Hello pretext")
                        .author_name("author. Panther azrael")
                        .author_link("https://aaa")
                        .title("This is title")
                        .text("Optional text at attachment")
                        .ts(Instant.now().toEpochMilli() / 1000)
                        .field(SlackMessage.Field.builder()
                                .title("FIELD---TITLE")
                                .value("FIELD___VALUE")
                                .build()
                        ).build())
                .channel("panther")
                .username("panther")
                .icon_emoji(":dizzy:")
                .build();

        String json = JsonUtil.toJson(slackAttachments);
        logger.info(json);

    }

    //@Test
    public void sendInfoTest() {
        assertNotNull(slackNotifier);
        slackNotifier.notify(SlackEvent.builder()
                .header("<TEST> This is header")
                .timestamp(Instant.now().toEpochMilli())
                .level(SlackMessage.LEVEL.INFO)
                .title("Point exchange succeed!!")
                .message("hello message")
                .details(ImmutableMap.of("key1", "value1", "key2", "value2"))
                .build());

    }

    //@Test
    public void sendWarnTest() {
        assertNotNull(slackNotifier);
        slackNotifier.notify(SlackEvent.builder()
                .header("<TEST> HappyPoint Warning")
                .timestamp(Instant.now().toEpochMilli())
                .level(SlackMessage.LEVEL.WARN)
                .title("Point not enough!!")
                .message("hello message")
                .details(ImmutableMap.of("key1", "value1", "key2", "value2"))
                .exception(new RuntimeException("run error"))
                .build());

    }

    //@Test
    public void sendErrorTest() {
        assertNotNull(slackNotifier);
        slackNotifier.notify(SlackEvent.builder()
                .header("<TEST> HappyPoint")
                .timestamp(Instant.now().toEpochMilli())
                .level(SlackMessage.LEVEL.ERROR)
                .title("Fraud Error !!")
                .message("ABC failed.")
                .details(ImmutableMap.of("key1", "value1", "key2", "value2"))
                .exception(new FraudException(Executor.Type.HAPPYPOINT, "Fraud. verify failed. "))
                .build());

    }

    //@Test
    public void sendErrorTestWithException() {
        assertNotNull(slackNotifier);

        RuntimeException re = new RuntimeException("test run failed");
        PantherException e1 = new PantherException(Executor.Type.HAPPYPOINT, re);
        logger.info("type = {}", e1.getExecutorType());
        assertNotNull(e1.getExecutorType());
        assertEquals(Executor.Type.HAPPYPOINT, e1.getExecutorType().get());
        PantherException e2 = new PantherException("hello, error");
        logger.info("type = {}", e2.getExecutorType());
        assertEquals(Optional.empty(), e2.getExecutorType());

        slackNotifier.notify(re);

        slackNotifier.notify(e1);

        when(mockContext.getUserId()).thenReturn(Optional.of(Long.valueOf(123L)));
        when(mockContext.getPaymentId()).thenReturn(Optional.of(Long.valueOf(9876L)));
        when(mockContext.getType()).thenReturn(Executor.Type.LGUDEPOSIT);
        when(mockContext.print()).thenReturn("[LGUDEPOSIT, u=123, p=9876]");
        SessionException sessionException = new SessionException(Executor.Type.LGUDEPOSIT,
                "Failed to get session");
        slackNotifier.notify(new InternalPaymentException(mockContext, sessionException));

    }

    /**
     * PantherException 들에 정의된 NotificationLevel 테스트.
     */
    @Test
    public void notificationLevelTest() {

        when(mockContext.getUserId()).thenReturn(Optional.of(Long.valueOf(123L)));
        when(mockContext.getPaymentId()).thenReturn(Optional.of(Long.valueOf(9876L)));
        when(mockContext.getType()).thenReturn(Executor.Type.HAPPYPOINT);
        when(mockContext.print()).thenReturn("[HAPPYPOINT, u=123, p=9876]");

        assertEquals(null, slackNotifier.levelOf(new CIException(Executor.Type.UNKNOWN, "notification")));
        assertEquals(null, slackNotifier.levelOf(new ExceedException(Executor.Type.UNKNOWN, "notification")));
        assertEquals(SlackMessage.LEVEL.ERROR,
                slackNotifier.levelOf(new ExecutorException(mockContext, "notification")));
        assertEquals(SlackMessage.LEVEL.ERROR,
                slackNotifier.levelOf(new FraudException(Executor.Type.UNKNOWN, "notification")));
        assertEquals(null, slackNotifier.levelOf(new HappyPointParamException(Executor.Type.HAPPYPOINT, "notification")));
        assertEquals(null, slackNotifier.levelOf(new HappyPointSystemException(mockContext, "notification")));
        assertEquals(SlackMessage.LEVEL.ERROR,
                slackNotifier.levelOf(new InternalPaymentException(mockContext, "notification")));
        assertEquals(SlackMessage.LEVEL.ERROR,
                slackNotifier.levelOf(new LguDepositException(mockContext, "notification")));
        assertEquals(SlackMessage.LEVEL.ERROR,
                slackNotifier.levelOf(new PantherException(Executor.Type.UNKNOWN, "notification")));
        assertEquals(SlackMessage.LEVEL.WARN,
                slackNotifier.levelOf(new ParameterException(Executor.Type.UNKNOWN, "notification")));
        assertEquals(null,
                slackNotifier.levelOf(new PreconditionException(mockContext, "notification")));
        assertEquals(SlackMessage.LEVEL.ERROR,
                slackNotifier.levelOf(new SessionException(Executor.Type.UNKNOWN, "notification")));

        assertEquals(SlackMessage.LEVEL.ERROR, slackNotifier.levelOf(new RuntimeException("hello")));
        assertEquals(SlackMessage.LEVEL.ERROR, slackNotifier.levelOf(new Throwable("hello")));

    }
}
