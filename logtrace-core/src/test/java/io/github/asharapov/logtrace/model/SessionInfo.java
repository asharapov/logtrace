package io.github.asharapov.logtrace.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Дескриптор описания пользовательских сессий при работе с демонстрационной системой
 * (в объеме необходимом для задач аудита и построения аналитических отчетов).
 *
 * @author Anton Sharapov
 */
public class SessionInfo {

    public enum Role {
        GUEST,
        USER,
        ADMIN
    }

    @JsonProperty("sid")
    public String sessionId;

    @JsonProperty("started")
    public Date started;

    @JsonProperty("principal")
    public String userName;

    @JsonProperty("role")
    public Role role;

    @JsonProperty("@time")
    public Long duration;
}
