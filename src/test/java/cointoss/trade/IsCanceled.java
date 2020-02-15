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

import org.junit.jupiter.api.Test;

public class IsCanceled extends TradingSituation {

    @Test
    @Override
    void entryOnly() {
        assert s.isCanceled() == false;
    }

    @Test
    @Override
    void entryExecutedPartially() {
        assert s.isCanceled() == false;
    }
}
