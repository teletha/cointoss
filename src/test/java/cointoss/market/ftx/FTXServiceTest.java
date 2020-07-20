/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.ftx;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;

class FTXServiceTest {

    @Test
    void latestExecution() {
        FTXServiceMock service = new FTXServiceMock();
        Execution latest = service.executionLatest().to().acquire();
    }
}
