/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.feather;

import org.junit.jupiter.api.Test;

public class EvictionPolicyTest {

    @Test
    void byLRU() {
        EvictionPolicy policy = EvictionPolicy.byLRU(3);

        assert policy.access(1) == -1; // 1
        assert policy.access(2) == -1; // 1,2
        assert policy.access(3) == -1; // 1,2,3
        assert policy.access(4) == 1; // 2,3,4
        assert policy.access(5) == 2; // 3,4,5
        assert policy.access(1) == 3; // 4,5,1
        assert policy.access(2) == 4; // 5,1,2
        assert policy.access(3) == 5; // 1,2,3
        assert policy.access(2) == -1; // 1,3,2
        assert policy.access(1) == -1; // 3,2,1
    }
}
