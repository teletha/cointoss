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

import static cointoss.MarketTestSupport.*;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/04/25 16:24:55
 */
public class PositionManagerTest {

    @Test
    void hasPosition() {
        PositionManager positions = new PositionManager();
        assert positions.hasPosition() == false;
        assert positions.hasNoPosition() == true;

        positions.add(Side.BUY, buy(10, 1));
        assert positions.hasPosition() == true;
        assert positions.hasNoPosition() == false;
    }

    @Test
    void isLong() {
        PositionManager positions = new PositionManager();
        assert positions.isLong() == false;
        assert positions.isShort() == false;

        positions.add(Side.BUY, buy(10, 1));
        assert positions.isLong() == true;
        assert positions.isShort() == false;
    }

    @Test
    void isShort() {
        PositionManager positions = new PositionManager();
        assert positions.isLong() == false;
        assert positions.isShort() == false;

        positions.add(Side.SELL, buy(10, 1));
        assert positions.isLong() == false;
        assert positions.isShort() == true;
    }
}
