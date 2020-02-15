/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import cointoss.Direction;
import cointoss.Directional;

class TradeSide implements Directional {

    public final Direction side;

    public final int sign;

    /**
     * @param side
     */
    private TradeSide(Direction side) {
        this.side = side;
        this.sign = side.isBuy() ? 1 : -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction direction() {
        return side;
    }

    static class Provider implements ArgumentsProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(Direction.values()).map(TradeSide::new).map(Arguments::arguments);
        }
    }
}
