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
 * @version 2018/05/02 23:19:35
 */

public class FcmPredictor {

    private long[] table;

    private int fcm_hash;

    public FcmPredictor(int logOfTableSize) {
        table = new long[1 << logOfTableSize];
    }

    public long getPrediction() {
        return table[fcm_hash];
    }

    public void update(long true_value) {
        table[fcm_hash] = true_value;
        fcm_hash = (int) (((fcm_hash << 6) ^ (true_value >> 48)) & (table.length - 1));
    }

}
