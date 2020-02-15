/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade.extension;

import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class PriceType {

    public final double entry;

    public final double exit;

    public final double diff;

    /**
     * @param entry
     * @param exit
     */
    public PriceType(double entry, double exit) {
        this.entry = entry;
        this.exit = exit;
        this.diff = exit - entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PriceType [entry=" + entry + ", exit=" + exit + "]";
    }

    /**
     * Collect all values.
     * 
     * @return
     */
    static List<PriceType> values() {
        return List.of(new PriceType(10, 20), new PriceType(20, 10), new PriceType(0.1, 0.2), new PriceType(0.2, 0.1));
    }

    private static final class Resolver implements ParameterResolver {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
                throws ParameterResolutionException {
            return parameterContext.getParameter().getType() == PriceType.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
                throws ParameterResolutionException {
            return new PriceType(10, 20);
        }
    }
}
