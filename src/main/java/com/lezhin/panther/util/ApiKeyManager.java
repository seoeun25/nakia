package com.lezhin.panther.util;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author seoeun
 * @since 2018.03.16
 */
@Component
public class ApiKeyManager {

    private static final int KEY_LENGTH = 16; // bytes

    // FIXME how to persist. how to manage.
    private static final Map<String, String> apiKeyMap = ImmutableMap.of(
            "lezhin", "20E6530ADA31EECE7AF3BAA8180A1109",
            "payletter", "A4A3683F36D4B93CFF3B4D591A59101F",
            "wincube", "E6570D8EFE1BD4B4A2751B19DF8F2CC0");

    public static String generate(byte[] seed) throws NoSuchAlgorithmException {
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
