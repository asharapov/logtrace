package io.github.asharapov.logtrace.tests.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Anton Sharapov
 */
public class ThrownInfo {

    @JsonProperty(required = true, value = "cls")
    public String cls;
    @JsonProperty(required = false, value = "msg")
    public String message;
    @JsonProperty(required = true, value = "stack")
    public String stack;
    @JsonProperty(required = true, value = "hash")
    public String hash;

}
