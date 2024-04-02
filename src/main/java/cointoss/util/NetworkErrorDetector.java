/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kiss.WiseFunction;

public class NetworkErrorDetector implements WiseFunction<Throwable, Throwable> {

    /** The keyword for authentication error. */
    private final Set<String> authentications = new HashSet();

    /** The keyword for maintenance error. */
    private final Set<String> maintenance = new HashSet();

    /** The keyword for rate-limit error. */
    private final Set<String> limit = new HashSet();

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable APPLY(Throwable e) throws Throwable {
        String message = e.getMessage().toLowerCase();

        for (String word : authentications) {
            if (message.contains(word)) {
                return new NetworkError.AuthenticationError(e);
            }
        }

        for (String word : limit) {
            if (message.contains(word)) {
                return new NetworkError.RateLimitError(e);
            }
        }

        for (String word : maintenance) {
            if (message.contains(word)) {
                return new NetworkError.MaintenanceError(e);
            }
        }
        return e;
    }

    public NetworkErrorDetector authentication(String... words) {
        authentications.addAll(List.of(words));
        return this;
    }

    public NetworkErrorDetector maintenance(String... words) {
        maintenance.addAll(List.of(words));
        return this;
    }

    public NetworkErrorDetector rateLimit(String... words) {
        limit.addAll(List.of(words));
        return this;
    }
}
