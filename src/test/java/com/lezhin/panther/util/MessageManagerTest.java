package com.lezhin.panther.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author taemmy
 * @since 2018. 6. 29.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class MessageManagerTest {

    @Autowired
    private MessageManager messageManager;

    @Test
    public void test_get_message() {
        String msgKey = "tapjoy.push.title";

        String msg = "레진코믹스";
        String msgEn = "Lezhin Comics";
        String msgJp = "レジンコミックス";

        // default
        assertEquals(msg, messageManager.get(msgKey));
        // ko_KR
        assertEquals(msg, messageManager.get(msgKey, Locale.KOREA));
        // en_US
        assertEquals(msgEn, messageManager.get(msgKey, Locale.US));
        // ja_JP
        assertEquals(msgJp, messageManager.get(msgKey, Locale.JAPAN));
    }

    @Test
    public void test_get_message_with_args() {
        String msgKey = "tapjoy.present.title";

        String msg = "16 보너스코인";
        String msgEn = "16 Bonus Coin";
        String msgJp = "16 ボーナスコイン";

        // ko_KR
        assertEquals(msg, messageManager.get(msgKey, Locale.KOREA, 16));
        // en_US
        assertEquals(msgEn, messageManager.get(msgKey, Locale.US, 16));
        // ja_JP
        assertEquals(msgJp, messageManager.get(msgKey, Locale.JAPAN, 16));

        String msgKey2 = "pincrux.purchase.title";
        String msg2 = "무료코인존: [테스트 이벤트]";

        assertEquals(msg2, messageManager.get(msgKey2, "테스트 이벤트"));


    }
}