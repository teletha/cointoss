/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
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
import kiss.Variable;

@Manageable(lifestyle = Singleton.class)
public abstract class MarketServiceProvider implements Extensible {

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
    protected MarketServiceProvider() {
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
     * Get the market account infomation.
     * 
     * @return
     */
    public abstract MarketAccount account();

    /**
     * Retrieve all {@link MarketServiceProvider}s.
     * 
     * @return
     */
    public static final Signal<MarketServiceProvider> availableProviders() {
        return I.signal(I.find(MarketServiceProvider.class)).take(market -> market.account().validate());
    }

    /**
     * Retrieve all {@link MarketService}s.
     * 
     * @return
     */
    public static final Signal<MarketService> availableMarketServices() {
        return availableProviders().flatIterable(MarketServiceProvider::markets);
    }

    /**
     * Retrieve by identical name.
     * 
     * @param identity
     * @return
     */
    public static final Variable<MarketService> by(String identity) {
        return availableMarketServices().take(service -> service.marketIdentity().equals(identity)).to();
    }
}
