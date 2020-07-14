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

import java.net.http.HttpRequest.Builder;

import kiss.I;
import kiss.Signal;

public class ErrorNetwork extends Network {

    /**
     * {@inheritDoc}
     */
    @Override
    public <M> Signal<M> rest(Builder request, APILimiter limiter, Class<M> type, String... selector) {
        return I.signalError(new Error());
    }
}