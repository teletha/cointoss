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
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import cointoss.Direction;

class Provider implements ArgumentsProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        Class<?>[] parameterTypes = context.getRequiredTestMethod().getParameterTypes();

        List<List> parameters = new ArrayList();
        for (Class<?> parameterType : parameterTypes) {
            if (parameterType == SideType.class) {
                parameters.add(SideType.values());
            } else if (parameterType == PriceType.class) {
                parameters.add(PriceType.values());
            }
        }

        List<Object[]> synthesized = new ArrayList();
        for (int i = 1; i < parameters.size(); i++) {
            List values = parameters.get(i);
            List<Arguments> synth = new ArrayList();

            for (Object value : values) {

            }
        }
        for (List list : parameters) {

        }

        return Stream.of(Direction.values()).map(SideType::new).map(Arguments::arguments);
    }
}