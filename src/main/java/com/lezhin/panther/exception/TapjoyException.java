package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author taemmy
 * @since 2018. 7. 5.
 */
@NotificationLevel(level = NotificationLevel.Level.WARN)
public class TapjoyException extends PantherException {
    /**
     * 만일 탭조이의 콜백 요청에 대해서 200 혹은 403 코드를 리턴해주지 않는다면 탭조이 서버가 4일 동안 2분간격으로 재시도 요청을 하게 됩니다.
     * 또한 서버 응답이 5초 이상 소요되면 해당 통신은 실패로 처리됩니다.
     * - 콜백 요청시 전달된 verifier 값과 secret key를 사용하여 직접 계산한 값이 일치하지 않을때
     * - snuid 값이 서버에서 식별되지 않을때
     * - 이외 보상 지급이 재시도되지 말아야할 오류 발생 시
     */
    public TapjoyException(String message) {
        super(message);
    }

    public TapjoyException(Executor.Type type, String message) {
        super(type, message);
    }

    public TapjoyException(Executor.Type type, Throwable e) {
        super(type, e);
    }
}
