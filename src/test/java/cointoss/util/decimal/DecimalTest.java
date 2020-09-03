/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.decimal;

import org.junit.jupiter.api.Test;

/**
 * 
 */
class DecimalTest {

    @Test
    void computeScale() {
        assert Decimal.computeScale(0.1) == 1;
        assert Decimal.computeScale(0.12) == 2;
        assert Decimal.computeScale(0.123) == 3;
        assert Decimal.computeScale(0.1234) == 4;
        assert Decimal.computeScale(0.100) == 1;
        assert Decimal.computeScale(0.02) == 2;
        assert Decimal.computeScale(0.02040) == 4;
    }
}
