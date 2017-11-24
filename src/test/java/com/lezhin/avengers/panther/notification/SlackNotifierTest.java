package com.lezhin.avengers.panther.notification;

import com.lezhin.avengers.panther.exception.InternalPaymentException;
import com.lezhin.avengers.panther.exception.PantherException;
import com.lezhin.avengers.panther.executor.Executor;
import com.lezhin.avengers.panther.util.JsonUtil;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * 실제로 slack (panther_beta)로 메시지가 전달되기 때문에 @Test 를 막아놓음. 필요시 풀어서 테스트. 
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

    //@Test
    public void test() {
        SlackMessage slackAttachments = SlackMessage.builder()
                .attachment(SlackMessage.Attachment.builder().fallback("fallback")
                        .pretext("Hello pretext")
                        .author_name("auther. Panther azrael")
                        .author_link("https://aaa")
                        .title("This is title")
                        .text("Optional text at attachment")
                        .ts(Instant.now().toEpochMilli() / 1000)
                        .field(SlackMessage.Field.builder()
                                .title("FIELD---TITLE")
                                .value("FIELD___VALUE")
                                .build()
                        ).build())
                .channel("panther_beta")
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
                .header("This is header. HappyPoint")
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
                .header("HappyPoint Warning")
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
                .header("HappyPoint")
                .timestamp(Instant.now().toEpochMilli())
                .level(SlackMessage.LEVEL.ERROR)
                .title("Internal Error !!")
                .message("InternalPayment failed.")
                .details(ImmutableMap.of("key1", "value1", "key2", "value2"))
                .exception(new InternalPaymentException(Executor.Type.HAPPYPOINT, "Internal.verify failed. "))
                .build());

    }

    //@Test
    public void sendErrorTestWithException() {
        assertNotNull(slackNotifier);

        RuntimeException re = new RuntimeException("test run failed");
        PantherException e = new PantherException(Executor.Type.HAPPYPOINT, re);
        slackNotifier.notify(SlackEvent.builder()
                .header(Optional.ofNullable(e.getType().name()).orElse("UnknownExecutor"))
                .level(SlackMessage.LEVEL.ERROR)
                .title(e.getMessage())
                .message(e.getMessage())
                .exception(e)
                .build());

        InternalPaymentException internalPaymentException = new InternalPaymentException(null, "Failed to send");
        slackNotifier.notify(SlackEvent.builder()
                .header(Optional.ofNullable(internalPaymentException.getType()).orElse(Executor.Type.DUMMY).name())
                .level(SlackMessage.LEVEL.ERROR)
                .title(internalPaymentException.getMessage())
                .message(internalPaymentException.getMessage())
                .exception(internalPaymentException)
                .build());

    }
}
