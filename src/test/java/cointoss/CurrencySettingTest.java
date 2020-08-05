/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import org.junit.jupiter.api.Test;

class CurrencySettingTest {

    @Test
    void scale() {
        assert Currency.UNKNOWN.minimumSize(1).scale == 0;
        assert Currency.UNKNOWN.minimumSize(0.1).scale == 1;
        assert Currency.UNKNOWN.minimumSize(0.01).scale == 2;
        assert Currency.UNKNOWN.minimumSize(10).scale == 0;
    }
}
