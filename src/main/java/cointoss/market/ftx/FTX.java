/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.ftx;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class FTX extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 10000;

    public static final MarketService ADA_PERP = new FTXService("ADA-PERP", MarketSetting.with.target(Currency.ADA.minimumSize(1))
            .base(Currency.USD.minimumSize(0.000005))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService AMPL_PERP = new FTXService("AMPL-PERP", MarketSetting.with.target(Currency.AMPL.minimumSize(1))
            .base(Currency.USD.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService BNB_PERP = new FTXService("BNB-PERP", MarketSetting.with.target(Currency.BNB.minimumSize(0.1))
            .base(Currency.USD.minimumSize(0.0005))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService BTC_PERP = new FTXService("BTC-PERP", MarketSetting.with.target(Currency.BTC.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.5))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService EOS_PERP = new FTXService("EOS-PERP", MarketSetting.with.target(Currency.EOS.minimumSize(0.1))
            .base(Currency.USD.minimumSize(0.00005))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService ETH_PERP = new FTXService("ETH-PERP", MarketSetting.with.target(Currency.ETH.minimumSize(0.001))
            .base(Currency.USD.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService LINK_PERP = new FTXService("LINK-PERP", MarketSetting.with.target(Currency.LINK.minimumSize(0.1))
            .base(Currency.USD.minimumSize(0.0005))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FTT_USDT = new FTXService("FTT/USDT", MarketSetting.with.target(Currency.FTT.minimumSize(1))
            .base(Currency.USDT.minimumSize(0.001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService YFI_USD = new FTXService("YFI/USD", MarketSetting.with.target(Currency.YFI.minimumSize(0.001))
            .base(Currency.USD.minimumSize(5))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService DEFI_PERP = new FTXService("DEFI-PERP", MarketSetting.with
            .target(Currency.FTX_DEFI.minimumSize(0.001))
            .base(Currency.USD.minimumSize(0.1))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService ALT_PERP = new FTXService("ALT-PERP", MarketSetting.with.target(Currency.FTX_ALT.minimumSize(0.001))
            .base(Currency.USD.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService MID_PERP = new FTXService("MID-PERP", MarketSetting.with.target(Currency.FTX_MID.minimumSize(0.001))
            .base(Currency.USD.minimumSize(0.1))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService SHIT_PERP = new FTXService("SHIT-PERP", MarketSetting.with
            .target(Currency.FTX_SHIT.minimumSize(0.001))
            .base(Currency.USD.minimumSize(0.1))
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(FTXAccount.class);
    }
}