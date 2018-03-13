package com.lezhin.panther.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author seoeun
 * @since 2017.11.05
 */
public class DateUtil {

    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    public static long ONE_SECOND = 1000;
    public static long ONE_MINUTE = ONE_SECOND * 60;
    public static long ONE_HOUR = ONE_MINUTE * 60;
    public static long ONE_DAY = ONE_HOUR * 24;

    public static ZoneId ASIA_SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    public static ZoneId UTC_ZONE = ZoneId.of("UTC");

    public static String DEFAULT_FORMAATER = "yyyyMMdd HH:mm:ss";
    public static String DATE_FORMATTER = "yyyyMMdd";
    public static String TIME_FORMATTER = "HHmmss";
    public static String DATE_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";


    /**
     * Return Date String on SEOUL. "yyyyMMdd"
     *
     * @param timestamp instant.toEpochMilli()
     * @return
     */
    public static String getDateString(long timestamp) {
        return format(timestamp, ASIA_SEOUL_ZONE, DATE_FORMATTER);
    }

    /**
     * Return Time String on SEOUL. "HHmmss"
     *
     * @param timestamp instant.now().toEpochMilli()
     * @return
     */
    public static String getTimeString(long timestamp) {
        return format(timestamp, ASIA_SEOUL_ZONE, TIME_FORMATTER);
    }

    public static String getDateTimeString(long timestamp) {
        return format(timestamp, ASIA_SEOUL_ZONE, DATE_FORMATTER + TIME_FORMATTER);
    }
    /**
     * Return the formatted String corresponding {@code timestamp}
     *
     * @param timestamp instant.toEpochMilli()
     * @param zoneId    ASIA_SEOUL_ZONE = ZoneId.of("Asia/Seoul")
     * @param pattern   "yyyyMMdd HH:mm:ss"
     * @return "20171118 23:10:11"
     */
    public static String format(long timestamp, ZoneId zoneId, String pattern) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        Instant instant = Instant.ofEpochMilli(timestamp);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);

        return formatter.format(zonedDateTime);
    }

    /**
     * Return the Instant corresponding {@code dateString} and {@code zoneId}
     *
     * @param dateTimeStr "2017-11-16 23:10:11"
     * @param pattern     "yyyy-MM-dd HH:mm:ss"
     * @param zoneId      ASIA_SEOUL_ZONE = ZoneId.of("Asia/Seoul")
     * @return
     */
    public static Instant toInstant(String dateTimeStr, String pattern, ZoneId zoneId) {

        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        return zonedDateTime.toInstant();
    }

    public static Instant toInstantFromDate(String dateStr, String pattern, ZoneId zoneId) {
        return toInstant(dateStr + " 00:00:00", pattern + " HH:mm:ss", zoneId);
    }

    public static String toDatePattern(String yyyyMMdd) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");

        try {
            Date dt = sf.parse(yyyyMMdd);
            sf.applyPattern("yyyy-MM-dd");
            return sf.format(dt);
        } catch (Exception e) {
            return yyyyMMdd;
        }
    }

    public static void printDate() {

        logger.info("system.current = {}", System.currentTimeMillis());
        logger.info("Instance.now   = {}", Instant.now().toEpochMilli());

        Instant now = Instant.now();
        Instant systemDefault = Instant.now(Clock.systemDefaultZone());
        Instant seoulInstant = Instant.now(Clock.system(DateUtil.ASIA_SEOUL_ZONE));
        Instant utcInstant = Instant.now(Clock.systemUTC());
        logger.info("system = {}", ZoneId.systemDefault().getId());

        logger.info("  now =           {}, {}", now.toEpochMilli(), now.toString());
        logger.info("  systemDefault = {}, {}", systemDefault.toEpochMilli(), systemDefault.toString());
        logger.info("  seoulInstant =  {}, {}", seoulInstant.toEpochMilli(), seoulInstant.toString());
        logger.info("  utcInstant =    {}, {}", utcInstant.toEpochMilli(), utcInstant.toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

        logger.info("seoul time = {}", DateUtil.format(now.toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE,
                "yyyyMMdd HH:mm:ss"));
        logger.info("utc   time = {}", DateUtil.format(now.toEpochMilli(), DateUtil.UTC_ZONE,
                "yyyyMMdd HH:mm:ss"));

        logger.info("UTC zonedDateTime = {}", ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli());
        logger.info("SEOUL zonedDateTime = {}", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli());

    }


}
