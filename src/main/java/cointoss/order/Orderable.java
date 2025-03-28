/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.util.function.Consumer;

/**
 * Both order strategy.
 */
public interface Orderable extends Takable, Makable {

    /**
     * Use the chained strategy.
     * 
     * @param strategy
     * @return
     */
    Orderable next(Consumer<Orderable> strategy);
}