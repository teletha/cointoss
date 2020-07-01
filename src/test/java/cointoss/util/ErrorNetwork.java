/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import com.google.gson.JsonElement;

import kiss.I;
import kiss.Signal;
import okhttp3.Request;

public class ErrorNetwork extends Network {

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<JsonElement> rest(Request request, APILimiter limiter) {
        return I.signalError(new Error());
    }
}