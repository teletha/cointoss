/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import cointoss.util.arithmetic.Num;
import kiss.Signal;

public abstract class ObservableNumProperty extends ObservableProperty<Num> {

    /**
     * Observe property diff.
     * 
     * @return
     */
    public Signal<Num> observe$Diff() {
        return observe$Now().pair(Num.ZERO).map(values -> values.ⅱ.minus(values.ⅰ));
    }
}