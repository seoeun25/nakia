package com.lezhin.panther.util;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author seoeun
 * @since 2018.03.16
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class ApiKeyManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyManagerTest.class);

    @Autowired
    private ApiKeyManager apiKeyManager;

    @Test
    public void testGenerate() throws NoSuchAlgorithmException {

        String lezhinApiKey = ApiKeyManager.generate("lezhin".getBytes());
        logger.info("lezhin={}", lezhinApiKey);

        String payletterApiKey = ApiKeyManager.generate("payletter".getBytes());
        logger.info("payletter=", payletterApiKey);

        /**
         * wincube 부터 환경별 api-key 를 분리
         */
        String company = "wincube";
        List<String> envs = Lists.newArrayList("alpah", "beta", "qa", "prod");
        for(String env : envs) {
            String apiKey = ApiKeyManager.generate(String.format("%s-%s", env, company).getBytes());
            logger.info("{}-{}={}", env, company, apiKey);
        }
    }

    @Test
    public void testValidate() {
        assertNotNull(apiKeyManager);

        //"lezhin", "20E6530ADA31EECE7AF3BAA8180A1109",
        // "payletter", "A4A3683F36D4B93CFF3B4D591A59101F"
        assertTrue(apiKeyManager.validate("payletter", "A4A3683F36D4B93CFF3B4D591A59101F"));
        assertTrue(apiKeyManager.validate("wincube", "E6570D8EFE1BD4B4A2751B19DF8F2CC0"));

        assertFalse(apiKeyManager.validate("lezhin", "A4A3683F36D4B93CFF3B4D591A59101F"));
        assertFalse(apiKeyManager.validate("payletter", "A4A3683F36D4B93CFF3B4D59EEEEEEE"));
        assertFalse(apiKeyManager.validate(null, null));
    }

}
