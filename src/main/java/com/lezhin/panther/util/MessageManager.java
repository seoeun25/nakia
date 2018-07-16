package com.lezhin.panther.util;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * @author taemmy
 * @since 2018. 6. 29.
 */
@Component
public class MessageManager {
    private MessageSource messageSource;

    public MessageManager(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(final String code) {
        return messageSource.getMessage(code, null, Locale.KOREA);
    }

    public String get(final String code, final Object... args) {
        return messageSource.getMessage(code, args, Locale.KOREA);
    }

    public String get(final String code, final Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

    public String get(final String code, final Locale locale, final Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    public Locale getLocale(final String locale) {
        if ("en-US".equalsIgnoreCase(locale)) {
            return Locale.US;
        } else if ("ja-JP".equalsIgnoreCase(locale)) {
            return Locale.JAPAN;
        }

        return Locale.KOREA;
    }
}
