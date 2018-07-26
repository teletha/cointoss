/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import java.lang.reflect.Modifier;
import java.util.List;

import cointoss.Market;
import cointoss.MarketService;
import kiss.Extensible;
import kiss.I;
import kiss.Manageable;
import kiss.Signal;
import kiss.Singleton;

/**
 * @version 2018/07/26 23:37:36
 */
@Manageable(lifestyle = Singleton.class)
public abstract class MarketProvider implements Extensible {

    /** The collection of markets. */
    private final List<MarketService> markets = I.signal(getClass().getFields())
            .take(field -> Modifier.isStatic(field.getModifiers()))
            .take(field -> MarketService.class.isAssignableFrom(field.getType()))
            .map(field -> field.get(null))
            .as(MarketService.class)
            .toList();

    /**
     * Initialization.
     */
    protected MarketProvider() {
    }

    /**
     * Retrieve all {@link Market}s.
     * 
     * @return
     */
    public final List<MarketService> markets() {
        return markets;
    }

    /**
     * Retrieve all {@link MarketProvider}s.
     * 
     * @return
     */
    public static final Signal<MarketProvider> availableProviders() {
        return I.signal(I.find(MarketProvider.class));
    }

    /**
     * Retrieve all {@link MarketProvider}s.
     * 
     * @return
     */
    public static final Signal<MarketService> availableMarkets() {
        return availableProviders().flatIterable(MarketProvider::markets);
    }
}
