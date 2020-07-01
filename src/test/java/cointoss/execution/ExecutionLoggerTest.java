/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import org.junit.jupiter.api.Test;

class ExecutionLoggerTest {

    @Test
    void encodeInt() {
        for (int i = 0; i <= 186; i++) {
            String encoded = ExecutionLogger.encodeInt(i);
            int decoded = ExecutionLogger.decodeInt(encoded.charAt(0));

            assert decoded == i;
        }
    }

    @Test
    void encodeLong() {
        for (long i = -3000; i <= 3000; i++) {
            String encoded = ExecutionLogger.encodeLong(i);
            long decoded = ExecutionLogger.decodeLong(encoded);

            assert decoded == i;
        }
    }
}