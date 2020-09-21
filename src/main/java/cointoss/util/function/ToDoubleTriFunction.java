/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.function;

public interface ToDoubleTriFunction<P1, P2, P3> {

    double applyAsDouble(P1 p1, P2 p2, P3 p3);
}