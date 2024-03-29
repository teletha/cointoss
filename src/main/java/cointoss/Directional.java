/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import hypatia.Orientational;

/**
 * @version 2017/08/20 18:46:21
 */
public interface Directional extends Orientational<Direction> {

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean isPositive() {
        return orientation() == Direction.BUY;
    }
}