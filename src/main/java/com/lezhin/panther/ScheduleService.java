package com.lezhin.panther;

import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.SlackEvent;
import com.lezhin.panther.notification.SlackMessage;
import com.lezhin.panther.notification.SlackNotifier;
import com.lezhin.panther.util.DateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author seoeun
 * @since 2018.01.23
 */
@Service
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final long RETENTION = (1000 * 60 * 60 * 24) * 7; // 7days

    private SlackNotifier slackNotifier;
    private PantherProperties pantherProperties;

    public ScheduleService(final PantherProperties pantherProperties, final SlackNotifier slackNotifier) {
        this.pantherProperties = pantherProperties;
        this.slackNotifier = slackNotifier;
    }


    @Scheduled(cron = "0 1 9 * * ?")
    public void cleanupLguplusLogs() {
        // FIXME lguplus log 를 삭제하지 말고 ES에 저장.
        logger.info("cleanup start");
        estimateVolume(new File(pantherProperties.getLguplus().getLogDir()));

        long base = Instant.now().toEpochMilli() - RETENTION;

        File logDir = new File(pantherProperties.getLguplus().getLogDir());
        if (logDir.exists() && logDir.isDirectory()) {
            String[] fileNames = logDir.list();
            Arrays.stream(fileNames).forEach(name -> logger.info(name));
            try {
                Arrays.stream(fileNames).filter(name -> extractDate(name).toEpochMilli() < base)
                        .map(name -> new File(logDir, name))
                        .forEach(file -> logger.info("delete {} = {} ", file.getName(), file.delete()));
            } catch (Exception e) {
                logger.warn("Failed to delete logFile", e);
                handlePantherException(new PantherException(Executor.Type.UNKNOWN, e));
            }
        }

        estimateVolume(new File(pantherProperties.getLguplus().getLogDir()));

    }

    /**
     * Extract the date from logFile.
     *
     * @param logName ex> log_20180122.log
     * @return Instant
     */
    private Instant extractDate(String logName) {
        Instant logDate = DateUtil.toInstantFromDate(logName.substring(4, 12), "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE);
        return logDate;

    }

    private void estimateVolume(File dir) {
        if (!dir.exists()) {
            logger.info("File not found : {}", dir.getAbsolutePath());
            return;
        }
        try {
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                long total = Arrays.stream(files).mapToLong(file -> file.length()).sum();
                logger.info("{}. files = {}, total size = {}", dir.getAbsolutePath(), files.length, total);
            }
        } catch (Throwable e) {
            logger.warn("Failed to getSize", e);
            // do nothing
        }

    }

    public void handlePantherException(final PantherException e) {
        logger.error("ScheduleService. PantherException", e);
        slackNotifier.notify(SlackEvent.builder()
                .header(Optional.ofNullable(e.getType()).orElse(Executor.Type.UNKNOWN).name())
                .level(SlackMessage.LEVEL.ERROR)
                .title(e.getMessage())
                .message("")
                .exception(e)
                .build());
    }

}
