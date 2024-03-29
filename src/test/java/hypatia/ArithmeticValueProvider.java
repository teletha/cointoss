/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package hypatia;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import com.google.common.collect.Sets;

import kiss.Variable;

class ArithmeticValueProvider implements ArgumentsProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        ArithmeticTest annotation = context.getRequiredTestMethod().getAnnotation(ArithmeticTest.class);

        List<Set<?>> parameters = new ArrayList();
        for (Class<?> parameterType : context.getRequiredTestMethod().getParameterTypes()) {
            if (parameterType == int.class) {
                parameters.add(IntStream.of(annotation.ints()).mapToObj(Integer::valueOf).collect(Collectors.toSet()));
            } else if (parameterType == long.class) {
                parameters.add(LongStream.of(annotation.longs()).mapToObj(Long::valueOf).collect(Collectors.toSet()));
            } else if (parameterType == double.class) {
                parameters.add(DoubleStream.of(annotation.doubles()).mapToObj(Double::valueOf).collect(Collectors.toSet()));
            } else if (parameterType == String.class) {
                parameters.add(Stream.of(annotation.strings()).collect(Collectors.toSet()));
            } else if (parameterType == Num.class) {
                parameters.add(Stream.of(annotation.strings()).map(Num::of).collect(Collectors.toSet()));
            } else if (parameterType == Variable.class) {
                parameters.add(Stream.of(annotation.strings()).map(Num::of).map(Variable::of).collect(Collectors.toSet()));
            } else if (parameterType == Orientational.class) {
                parameters.add(Set.of(Orientational.POSITIVE, Orientational.NEGATIVE));
            }
        }
        return Sets.cartesianProduct(parameters).stream().map(List::toArray).map(Arguments::arguments);
    }

}