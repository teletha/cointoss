/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import cointoss.execution.Execution;
import cointoss.order.Order;
import kiss.Signal;

public abstract class Trading {

    protected abstract void createEntry(NewEntry entry);

    protected abstract Signal<Order> createExit(Order entry, Execution exe);

    protected abstract Signal<Order> createLossCut(Order entry, Execution exe);
}
