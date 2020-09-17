/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.primitive;

public interface LongBiFunction<V, R> {

    /**
     * Composes a single value from three arguments.
     * 
     * @param param1 First parameter.
     * @param param2 Second parameter.
     * @return A calculated result.
     */
    R apply(long param1, V param2);
}