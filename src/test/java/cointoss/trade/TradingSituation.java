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

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import cointoss.Scenario;

abstract class TradingSituation implements BeforeEachCallback {

    /** The latest scenario. */
    protected Scenario s;

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        System.out.println(context);
    }

    abstract void entryOnly();

    abstract void entryExecutedPartially();
}
