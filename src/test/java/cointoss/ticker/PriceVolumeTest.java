/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import org.junit.jupiter.api.Test;

import cointoss.util.Num;

class PriceVolumeTest {

    @Test
    void volumeAt() {
        PriceVolume volume = new PriceVolume(Num.of(200), Num.of(10));
        volume.update(Num.of(200), 1);
        assert volume.volumeAt(200) == 1;
    }
}
