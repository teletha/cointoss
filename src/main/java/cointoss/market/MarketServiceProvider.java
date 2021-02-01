/*
 * Copyright (C) 2021 cointoss Development Team
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
import kiss.Decoder;
import kiss.Encoder;
import kiss.Extensible;
import kiss.I;
import kiss.Managed;
import kiss.Signal;
import kiss.Singleton;
import kiss.Variable;

@Managed(value = Singleton.class)
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
        return I.signal(I.find(MarketServiceProvider.class));
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
        return availableMarketServices().take(service -> service.id.equalsIgnoreCase(identity)).to();
    }

    /**
     * Codec for {@link MarketService}.
     */
    static class Codec implements Decoder<MarketService>, Encoder<MarketService> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(MarketService value) {
            return value.id;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MarketService decode(String value) {
            return by(value).exact();
        }
    }
}