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

/**
 * Taker order strategy.
 */
public interface Takable {

    /**
     * Market order.
     * 
     * @return Taker is NOT cancellable.
     */
    Orderable take();
}