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
import java.util.Objects;
import java.util.Set;

import cointoss.market.Exchange;
import kiss.WiseFunction;

public class NetworkErrorDetector implements WiseFunction<Throwable, Throwable> {

    /** The keyword for authentication error. */
    private final Set<String> auth = new HashSet();

    /** The keyword for maintenance error. */
    private final Set<String> maintenance = new HashSet();

    /** The keyword for rate-limit error. */
    private final Set<String> limit = new HashSet();

    /** The keyword for minimum order size error. */
    private final Set<String> min = new HashSet();

    /** The associated exchange. */
    private final Exchange exchange;

    public NetworkErrorDetector(Exchange exchange) {
        this.exchange = Objects.requireNonNull(exchange);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable APPLY(Throwable e) throws Throwable {
        String message = e.getMessage().toLowerCase();

        for (String word : auth) {
            if (message.contains(word)) {
                return new NetworkError.UnauthenticatedAccess(e, exchange);
            }
        }

        for (String word : limit) {
            if (message.contains(word)) {
                return new NetworkError.APILimitOverflow(e, exchange);
            }
        }

        for (String word : maintenance) {
            if (message.contains(word)) {
                return new NetworkError.MarketMaintenance(e, exchange);
            }
        }

        for (String word : min) {
            if (message.contains(word)) {
                return new NetworkError.MinimumOrder(e, exchange);
            }
        }
        return e;
    }

    /**
     * Register the error words.
     * 
     * @param words
     * @return
     */
    public NetworkErrorDetector authentication(String... words) {
        auth.addAll(List.of(words));
        return this;
    }

    /**
     * Register the error words.
     * 
     * @param words
     * @return
     */
    public NetworkErrorDetector maintenance(String... words) {
        maintenance.addAll(List.of(words));
        return this;
    }

    /**
     * Register the error words.
     * 
     * @param words
     * @return
     */
    public NetworkErrorDetector limitOverflow(String... words) {
        limit.addAll(List.of(words));
        return this;
    }

    /**
     * Register the error words.
     * 
     * @param words
     * @return
     */
    public NetworkErrorDetector minimumOrder(String... words) {
        min.addAll(List.of(words));
        return this;
    }
}
