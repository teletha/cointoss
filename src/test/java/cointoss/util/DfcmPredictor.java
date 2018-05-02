/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

/**
 * @version 2018/05/02 23:19:16
 */
public class DfcmPredictor {

    private long[] table;

    private int dfcm_hash;

    private long lastValue;

    public DfcmPredictor(int logOfTableSize) {
        table = new long[1 << logOfTableSize];
    }

    public long getPrediction() {
        return table[dfcm_hash] + lastValue;
    }

    public void update(long true_value) {
        table[dfcm_hash] = true_value - lastValue;
        dfcm_hash = (int) (((dfcm_hash << 2) ^ ((true_value - lastValue) >> 40)) & (table.length - 1));
        lastValue = true_value;
    }

}
