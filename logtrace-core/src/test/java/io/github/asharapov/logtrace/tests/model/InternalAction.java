package io.github.asharapov.logtrace.tests.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Дескриптор работы с хранилищем данных
 * (в объеме необходимом для задач аудита и построения аналитических отчетов).
 *
 * @author Anton Sharapov
 */
public class InternalAction {
    @JsonProperty("name")
    public String name;
    @JsonProperty("args")
    public String args;

    @JsonProperty("@time")
    public Long duration;
}
