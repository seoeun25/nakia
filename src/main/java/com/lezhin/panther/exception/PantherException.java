package com.lezhin.panther.exception;

import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

import java.util.Optional;

/**
 * Panther의 최상위 Exception.
 *
 * @author seoeun
 * @since 2017.10.24
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class PantherException extends RuntimeException {

    protected Optional<Context> context = Optional.empty();
    protected Optional<Executor.Type> executorType;

    public PantherException(String message) {
        super(message);
    }

    public PantherException(Throwable e) {
        super(e);
    }

    public PantherException(Context context, String message) {
        super(context.print() + ": " + message);
        this.context = Optional.of(context);
    }

    public PantherException(Context context, Throwable e) {
        super(context.print(), e);
        this.context = Optional.of(context);
    }

    public PantherException(Context context, String message, Throwable e) {
        super(context.print() + ": " + message, e);
        this.context = Optional.of(context);
    }

    public PantherException(Executor.Type type, Throwable e) {
        super(e);
        this.executorType = Optional.of(type);
    }

    public PantherException(Executor.Type type, String message) {
        super(message);
        this.executorType = Optional.of(type);
    }

    public PantherException(Executor.Type type, String message, Throwable e) {
        super(message, e);
        this.executorType = Optional.of(type);
    }

    public Optional<Context> getContext() {
        return context;
    }

    public Optional<Executor.Type> getExecutorType() {
        return Optional.ofNullable(executorType).orElse(context.map(c -> c.getType()));
    }
}
