/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.volume;

import org.junit.jupiter.api.Test;

import cointoss.util.arithmetic.Num;
import cointoss.volume.PriceRangedVolumeManager.PriceRangedVolumePeriod;

class PriceVolumeTest {

    @Test
    void volumeAt() {
        PriceRangedVolumeManager manager = new PriceRangedVolumeManager(Num.of(10));
        PriceRangedVolumePeriod volume = manager.createPeriod(2000);
        for (int i = 0; i < 4000; i++) {
            volume.update(Num.of(i), 1);
        }

        for (int i = 0; i < 4000; i++) {
            assert volume.volumeAt(i) == 10;
        }
    }

    @Test
    void volumeAtWithDecimal() {
        PriceRangedVolumeManager manager = new PriceRangedVolumeManager(Num.of(0.001));
        PriceRangedVolumePeriod volume = manager.createPeriod(0.2);
        for (int i = 0; i < 4000; i++) {
            volume.update(Num.of(i * 0.0001), 1);
        }

        for (int i = 0; i < 4000; i++) {
            assert volume.volumeAt(i * 0.0001F) == 10;
        }
    }
}