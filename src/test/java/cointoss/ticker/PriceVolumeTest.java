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
        PriceRangedVolume volume = new PriceRangedVolume(0, Num.of(2000), Num.of(10), 0);
        for (int i = 0; i < 4000; i++) {
            volume.update(Num.of(i), 1);
        }

        for (int i = 0; i < 4000; i++) {
            assert volume.volumeAt(i) == 10;
        }
    }

    @Test
    void volumeAtWithDecimal() {
        PriceRangedVolume volume = new PriceRangedVolume(0, Num.of(0.2), Num.of(0.001), 5);
        for (int i = 0; i < 4000; i++) {
            volume.update(Num.of(i * 0.0001), 1);
        }

        for (int i = 0; i < 4000; i++) {
            assert volume.volumeAt(i * 0.0001) == 10;
        }
    }
}
