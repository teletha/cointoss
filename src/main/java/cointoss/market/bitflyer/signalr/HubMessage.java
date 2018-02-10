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
 * @version 2018/02/10 14:54:07
 */
public class HubMessage {
    private String invocationId = "";

    private String target = "";

    private JsonElement[] arguments;

    public HubMessage(String invocationId, String target, JsonElement[] arguments) {
        this.invocationId = invocationId;
        this.target = target;
        this.arguments = arguments;
    }

    public String getInvocationId() {
        return invocationId;
    }

    public void setInvocationId(String invocationId) {
        this.invocationId = invocationId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public JsonElement[] getArguments() {
        return arguments;
    }

    public void setArguments(JsonElement[] arguments) {
        this.arguments = arguments;
    }
}