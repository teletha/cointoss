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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import com.google.common.collect.Sets;

class TradePartProvider implements ArgumentsProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        List<Set<?>> parameters = new ArrayList();
        for (Class<?> parameterType : context.getRequiredTestMethod().getParameterTypes()) {
            if (parameterType == SidePart.class) {
                parameters.add(SidePart.values());
            } else if (parameterType == PricePart.class) {
                parameters.add(PricePart.values());
            } else if (parameterType == SizePart.class) {
                parameters.add(SizePart.values());
            }
        }
        return Sets.cartesianProduct(parameters).stream().map(List::toArray).map(Arguments::arguments);
    }
}