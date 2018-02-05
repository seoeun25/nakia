package com.lezhin.panther.notification;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This meta info is for the notification level.
 * <p>If the level is Error, the consumer should handle the error.
 * ex> SlackNotifier send the error message to slack.
 * </p>
 *
 * @author seoeun
 * @since 2018.02.07
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotificationLevel {

    enum Level {NONE, INFO, WARN, ERROR}

    Level level() default Level.NONE;
}
