package com.lezhin.panther.util;

import com.google.common.collect.ImmutableMap;
import com.lezhin.panther.config.PantherProperties;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;

/**
 * @author seoeun
 * @since 2018.03.16
 */
@Component
public class ApiKeyManager {

    private static final int KEY_LENGTH = 16; // bytes

    private static Map<String, String> apiKeyMap = ImmutableMap.of("lezhin", "20E6530ADA31EECE7AF3BAA8180A1109");
    public ApiKeyManager(final PantherProperties pantherProperties) {
        if(pantherProperties.getApiKey() != null){
            apiKeyMap = pantherProperties.getApiKey();
        }
    }

    public static String generate(byte[] seed) throws NoSuchAlgorithmException {
        /**
         * wincube 부터 환경별 api-key 분리
         * (lezhin, payletter 는 기존과 동일)
         */
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG"); //Pseudo-Random Number Generator
        sr.setSeed(seed);
        kgen.init(KEY_LENGTH * 8, sr); // bits
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return DatatypeConverter.printHexBinary(raw);
    }

    public boolean validate(String clientName, String apiKey) {
        return apiKeyMap.entrySet().stream()
                .anyMatch(entry -> (entry.getKey().equals(clientName) && entry.getValue().equals(apiKey)));
    }

}
