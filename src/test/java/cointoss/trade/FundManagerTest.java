/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import org.junit.jupiter.api.Test;

class FundManagerTest {

    @Test
    void riskAssets() {
        assert FundManager.with.totalAssets(100).riskAssetsRatio(0.01).riskAssets().is(1);
        assert FundManager.with.totalAssets(100).riskAssetsRatio(0.02).riskAssets().is(2);
        assert FundManager.with.totalAssets(10000).riskAssetsRatio(0).riskAssets().is(10);
        assert FundManager.with.totalAssets(10000).riskAssetsRatio(1).riskAssets().is(500);
    }
}
