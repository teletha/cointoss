/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import cointoss.MarketService;
import cointoss.util.NestableExtension;
import psychopath.Locator;

public class TestableExecutionLog extends ExecutionLog implements NestableExtension {

    @RegisterExtension
    private static CleanRoom room = new CleanRoom();

    /**
     * @param service
     */
    public TestableExecutionLog(MarketService service) {
        super(service, Locator.directory(room.root));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        System.out.println("OK Log");
    }

}
