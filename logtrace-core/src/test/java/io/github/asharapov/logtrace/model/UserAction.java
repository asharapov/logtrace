package io.github.asharapov.logtrace.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Дескриптор бизнес-операций выполнение которых было инициировано пользователем
 * (в объеме необходимом для задач аудита и построения аналитических отчетов).
 *
 * @author Anton Sharapov
 */
public class UserAction {
    public String uid;
    public String name;
    public String arg1;
    public String arg2;

    @JsonProperty("@time")
    public Long duration;
}
