/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import kiss.Signal;

public abstract class ObservableNumProperty extends ObservableProperty<Num> {

    /**
     * Observe property diff.
     * 
     * @return
     */
    public Signal<Num> observe$Diff() {
        return observe$Now().maps(Num.ZERO, (prev, now) -> now.minus(prev));
    }
}
