/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import org.apache.commons.lang3.RandomStringUtils;

import cointoss.execution.ExecutionLog;
import cointoss.execution.TestableExecutionLog;
import cointoss.verify.VerifiableMarketService;

public class TestableMarketService extends VerifiableMarketService {

    /** Expose the testable object. */
    public TestableExecutionLog log;

    public TestableMarketService() {
        super(RandomStringUtils.randomAlphabetic(20));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExecutionLog createExecutionLog() {
        return log = new TestableExecutionLog(this);
    }
}
