/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import kiss.Manageable;
import kiss.Singleton;
import kiss.Storable;
import kiss.Variable;
import stylist.value.Color;

/**
 * @version 2018/09/10 22:17:23
 */
@Manageable(lifestyle = Singleton.class)
public class Theme implements Storable<Theme> {

    /* Long position color. */
    public final Variable<Color> Long = Variable.of(Color.rgb(251, 189, 42));

    /* Short position color. */
    public final Variable<Color> Short = Variable.of(Color.rgb(247, 105, 77));

    /**
     * 
     */
    private Theme() {
        restore().auto();
    }
}
