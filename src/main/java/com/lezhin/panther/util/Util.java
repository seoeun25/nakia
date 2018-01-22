package com.lezhin.panther.util;

import com.lezhin.beans.entity.common.LezhinLocale;
import com.lezhin.constant.PaymentType;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.executor.Executor;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author seoeun
 * @since 2017.11.07
 */
public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    /**
     * Merge the {@code update} VO to {@code base}.
     * If the property is in base VO and update VO both,
     * will be overwritten by updatedVO's.
     *
     * @param base
     * @param update
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T merge(final T base, final T update, final Class<T> type) {

        T target;
        try {
            Class<T> claz = (Class<T>) Class.forName(type.getName());
            target = claz.newInstance();
            copy(base, target, type);
            copy(update, target, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge VO", e);
        }
        return target;

    }

    private static <T> void copy(T source, T target, Class<T> type) throws IntrospectionException, InvocationTargetException, IllegalAccessException {

        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isFinal(field.getModifiers())) {
                Method getMethod = new PropertyDescriptor(field.getName(), type).getReadMethod();

                Object obj1 = getMethod.invoke(source);
                if (obj1 != null) {

                    Method setMethod = new PropertyDescriptor(field.getName(), type).getWriteMethod();
                    setMethod.invoke(target, obj1);
                }
            }
        }

    }

    public static LezhinLocale of(String locale) {
        if (LezhinLocale.KO_KR.getId().equals(locale)) {
            return LezhinLocale.KO_KR;
        } else if (LezhinLocale.EN_US.getId().equals(locale)) {
            return LezhinLocale.EN_US;
        } else if (LezhinLocale.JA_JP.getId().equals(locale)) {
            return LezhinLocale.JA_JP;
        } else {
            throw new RuntimeException("Unknown locale = " + locale);
        }
    }

    public static String getLang(String locale) {
        String lang = Optional.ofNullable(locale).filter(e -> e.length() > 2).map(e -> e.substring(0,2)).orElse("ko");
        return lang;
    }

    public static String loadVersion() {
        Resource resource = new ClassPathResource("/version.txt");

        try {
            String result = CharStreams.toString(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
            logger.info("result = {}", result);

            return result;
        } catch (IOException e) {
            logger.warn("Failed to read version.txt", e);
        }
        return null;
    }

    public static String convertEncoding(String source, String sourceEncoding, String targetEncoding) {
        if (source == null) {
            return null;
        }
        byte[] euckrStringBuffer  = source.getBytes(Charset.forName(sourceEncoding));
        String decodedHelloString = null;
        try {
            decodedHelloString = new String(euckrStringBuffer, targetEncoding);
        } catch (Exception e) {
            logger.warn("Failed to convertEncoding: " + e.getMessage());
            return source;
        }
        return decodedHelloString;
    }

    public static Executor.Type getType(Throwable e) {
        Executor.Type executorType = Optional.of(e).filter(ex -> (ex instanceof PantherException))
                .map(ex -> ((PantherException)ex).getType()).orElse(Executor.Type.DUMMY);
        return executorType;
    }

}
