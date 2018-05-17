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
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import cointoss.backtest.TestableMarket;
import cointoss.util.Chrono;

/**
 * @version 2018/05/08 13:09:15
 */
class MarketLogTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    TestableMarket market = new TestableMarket();

    MarketLog log = new MarketLog(market.service, room.root);

    @Test
    void logAtNoServicedDate() {
        assert log.at(2016, 1, 1).toList().isEmpty();
    }

    @Test
    void logAtServicedDateWithoutExecutions() {
        assert log.at(Chrono.utcNow()).toList().isEmpty();
    }
}
