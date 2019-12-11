/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

import cointoss.Trader;
import kiss.I;
import kiss.Ⅱ;
import kiss.model.Model;
import kiss.model.Property;

class ParameterizedTraderBuilder {

    /** The base trader. */
    final Trader trader;

    /** The trader class. */
    final Class<Trader> clazz;

    /** The trader model. */
    final Model<Trader> model;

    /** The value holder. */
    final Map<Property, List<Object>> propertyValues = new HashMap();

    /**
     * @param clazz
     */
    protected ParameterizedTraderBuilder(Trader trader) {
        this.trader = trader;
        this.clazz = (Class<Trader>) trader.getClass();
        this.model = Model.of(clazz);
    }

    /**
     * Build all combined {@link Trader}s.
     * 
     * @return
     */
    List<Trader> build() {
        List<Set<Ⅱ<Property, Object>>> combinations = new ArrayList();

        for (Entry<Property, List<Object>> entry : propertyValues.entrySet()) {
            combinations.add(I.signal(entry.getValue()).map(v -> I.pair(entry.getKey(), v)).toCollection(new LinkedHashSet<>()));
        }

        List<Trader> traders = new ArrayList();

        for (List<Ⅱ<Property, Object>> combined : Sets.cartesianProduct(combinations)) {
            Trader trader = I.make(clazz);

            for (Ⅱ<Property, Object> value : combined) {
                model.set(trader, value.ⅰ, value.ⅱ);
            }
            traders.add(trader);
        }
        return traders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return trader.name();
    }
}
