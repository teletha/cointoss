/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import hypatia.Num;
import kiss.Signal;

public abstract class ObservableNumProperty extends ObservableProperty<Num> {

    /**
     * Observe property diff.
     * 
     * @return
     */
    public Signal<Num> observe$Diff() {
        return observe$Now().startWith(Num.ZERO).buffer(2, 1).map(values -> values.get(1).minus(values.get(0)));
    }
}