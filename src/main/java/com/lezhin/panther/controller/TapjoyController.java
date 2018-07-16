package com.lezhin.panther.controller;

import com.lezhin.panther.exception.TapjoyException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.pg.tapjoy.TapjoyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author taemmy
 * @since 2018. 6. 27.
 */
@RestController
@RequestMapping("/tapjoy/v1")
public class TapjoyController {
    private static final Logger logger = LoggerFactory.getLogger(TapjoyController.class);

    private TapjoyService tapjoyService;

    public TapjoyController(TapjoyService tapjoyService) {
        this.tapjoyService = tapjoyService;
    }

    /**
     * locale, platform 별 URL 이 필요한 상태
     */
    @GetMapping("/postback/{locale}/{platform}")
    public ResponseEntity<String> postback(
            @PathVariable(value = "locale") String locale,
            @PathVariable(value = "platform") String platform,
            @RequestParam Map<String, String> params) {
        /**
         * {mac_address=,
         *  verifier=92fe14ef7fc9cce4f404037a5a982430,
         *  currency=1,
         *  id=91b6051c-7d1f-47f1-82c2-6fa3ea49dc0e,
         *  snuid=5301167082700800,
         *  display_multiplier=1.0}
         *
         *  verifier = md5Hash(#{id}:#{snuid}:#{currency}:#{secret_key})
         */
        logger.info("postback - locale: {}, platform: {}, params: {}", locale, platform, params);
        tapjoyService.postback(locale, platform, params);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
