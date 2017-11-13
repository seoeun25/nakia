package com.lezhin.avengers.panther.util;

import com.lezhin.beans.entity.common.LezhinLocale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
}
