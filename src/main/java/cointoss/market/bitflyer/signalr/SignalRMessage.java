/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer.signalr;

import com.google.gson.JsonElement;

/**
 * @version 2018/02/10 14:55:09
 */
public class SignalRMessage {
    private String invocationId;

    private Integer type;

    private String target;

    private Boolean nonBlocking;

    private JsonElement[] arguments;

    public String getInvocationId() {
        return invocationId;
    }

    public void setInvocationId(String invocationId) {
        this.invocationId = invocationId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Boolean getNonBlocking() {
        return nonBlocking;
    }

    public void setNonBlocking(Boolean nonBlocking) {
        this.nonBlocking = nonBlocking;
    }

    public JsonElement[] getArguments() {
        return arguments;
    }

    public void setArguments(JsonElement[] arguments) {
        this.arguments = arguments;
    }
}