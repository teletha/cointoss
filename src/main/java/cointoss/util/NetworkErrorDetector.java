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

import java.util.List;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import cointoss.MarketService;
import cointoss.util.NetworkError.Kind;

public class NetworkErrorDetector {

    /** The keyword for various errors. */
    private final Multimap<Kind, String> keywords = MultimapBuilder.enumKeys(Kind.class).arrayListValues().build();

    /**
     * Convert to {@link NetworkError} if available.
     * 
     * @param e
     * @param market
     * @return
     */
    public Throwable convert(Throwable e, MarketService market) {
        if (e instanceof NetworkError) {
            return e;
        }

        String message = e.getMessage().toLowerCase();

        for (Kind kind : Kind.values()) {
            for (String word : keywords.get(kind)) {
                if (message.contains(word)) {
                    return new NetworkError(kind, e, market);
                }
            }
        }
        return e;
    }

    /**
     * Convert to {@link NetworkError} if available.
     * 
     * @param message
     * @param market
     * @return
     */
    public Throwable convert(String message, MarketService market) {
        for (Kind kind : Kind.values()) {
            for (String word : keywords.get(kind)) {
                if (message.contains(word)) {
                    return new NetworkError(kind, message, market);
                }
            }
        }
        return new NetworkError(Kind.Unkwnow, message, market);
    }

    /**
     * Register the error words.
     * 
     * @param words
     * @return
     */
    public NetworkErrorDetector register(Kind kind, String... words) {
        keywords.putAll(kind, List.of(words));
        return this;
    }
}
