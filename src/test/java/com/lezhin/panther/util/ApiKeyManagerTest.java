package com.lezhin.panther.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        logger.info(lezhinApiKey);

        String payletterApiKey = ApiKeyManager.generate("payletter".getBytes());
        logger.info(payletterApiKey);

        String wincubeApiKey = ApiKeyManager.generate("wincube".getBytes());
        logger.info(wincubeApiKey);

    }

    @Test
    public void testValidate() {
        assertNotNull(apiKeyManager);

        //"lezhin", "20E6530ADA31EECE7AF3BAA8180A1109",
        // "payletter", "A4A3683F36D4B93CFF3B4D591A59101F"

        assertEquals(true, apiKeyManager.validate("payletter", "A4A3683F36D4B93CFF3B4D591A59101F"));
        assertEquals(false, apiKeyManager.validate("payletter", "A4A3683F36D4B93CFF3B4D59EEEEEEE"));
        assertEquals(false, apiKeyManager.validate(null, null));

        assertEquals(false, apiKeyManager.validate("lezhin", "A4A3683F36D4B93CFF3B4D591A59101F"));
    }

}
