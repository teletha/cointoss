/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.gmo;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import cointoss.execution.LogHouse;

public class LogHouseTest {

    @Test
    void has() {
        LogHouse house = GMO.BTC.loghouse();
        assert house.has(LocalDate.of(2024, 1, 1)) == true;
        assert house.has(LocalDate.of(1990, 1, 1)) == false;
    }
}