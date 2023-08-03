/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade.extension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
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
        TradeTest annotation = context.getRequiredTestMethod().getAnnotation(TradeTest.class);

        List<Set<?>> parameters = new ArrayList();
        for (Class<?> parameterType : context.getRequiredTestMethod().getParameterTypes()) {
            if (parameterType == SidePart.class) {
                parameters.add(Stream.of(annotation.side()).map(SidePart::new).collect(Collectors.toSet()));
            } else if (parameterType == PricePart.class) {
                Set<PricePart> set = new HashSet();
                double[] prices = annotation.price();
                for (int i = 0; i < prices.length; i += 2) {
                    set.add(new PricePart(prices[i], prices[i + 1]));
                }
                parameters.add(set);
            } else if (parameterType == SizePart.class) {
                parameters.add(DoubleStream.of(annotation.size()).mapToObj(SizePart::new).collect(Collectors.toSet()));
            } else if (parameterType == ScenePart.class) {
                ScenePart[] scenes = annotation.scene();
                if (scenes.length == 0) {
                    scenes = ScenePart.values();
                }
                parameters.add(Set.of(scenes));
            } else if (parameterType == HoldTimePart.class) {
                parameters.add(IntStream.of(annotation.gap()).mapToObj(HoldTimePart::new).collect(Collectors.toSet()));
            } else if (parameterType == StrategyPart.class) {
                parameters.add(Set.of(StrategyPart.values()));
            }
        }
        return Sets.cartesianProduct(parameters).stream().map(List::toArray).map(Arguments::arguments);
    }
}