/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import cointoss.util.arithmetic.Num;

public class DivisionTest {

    @Test
    void weight() {
        for (Division division : Division.values()) {
            assert Stream.of(division.weights).mapToDouble(Num::doubleValue).sum() == 1d;
        }
    }
}
