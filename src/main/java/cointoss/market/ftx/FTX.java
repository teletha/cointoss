/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.ftx;

import cointoss.market.Exchange;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

final class FTX extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 100;
    //
    // static final MarketService ADA_PERP = new FTXService("ADA-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.ADA.minimumSize(1))
    // .base(Currency.USD.minimumSize(0.000005))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService AMPL_PERP = new FTXService("AMPL-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.AMPL.minimumSize(1))
    // .base(Currency.USD.minimumSize(0.0001))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService ATOM_PERP = new FTXService("ATOM-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.ATOM.minimumSize(0.01))
    // .base(Currency.USD.minimumSize(0.0005))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService AVAX_PERP = new FTXService("AVAX-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.AVAX.minimumSize(0.1))
    // .base(Currency.USD.minimumSize(0.001))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService BNB_PERP = new FTXService("BNB-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.BNB.minimumSize(0.1))
    // .base(Currency.USD.minimumSize(0.0005))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService BTC_PERP = new FTXService("BTC-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.BTC.minimumSize(0.0001))
    // .base(Currency.USD.minimumSize(0.5))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService BTC_USD = new FTXService("BTC/USD", MarketSetting.with.spot()
    // .target(Currency.BTC.minimumSize(0.0001))
    // .base(Currency.USDT.minimumSize(1))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService BTC_USDT = new FTXService("BTC/USDT", MarketSetting.with.spot()
    // .target(Currency.BTC.minimumSize(0.0001))
    // .base(Currency.USDT.minimumSize(1))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService EOS_PERP = new FTXService("EOS-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.EOS.minimumSize(0.1))
    // .base(Currency.USD.minimumSize(0.00005))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService ETH_PERP = new FTXService("ETH-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.ETH.minimumSize(0.001))
    // .base(Currency.USD.minimumSize(0.01))
    // .priceRangeModifier(100)
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService ETH_USDT = new FTXService("ETH/USDT", MarketSetting.with.spot()
    // .target(Currency.ETH.minimumSize(0.001))
    // .base(Currency.USDT.minimumSize(0.1))
    // .priceRangeModifier(100)
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService LINK_PERP = new FTXService("LINK-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.LINK.minimumSize(0.1))
    // .base(Currency.USD.minimumSize(0.0005))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService SOL_PERP = new FTXService("SOL-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.SOL.minimumSize(0.01))
    // .base(Currency.USD.minimumSize(0.0005))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService COMP_PERP = new FTXService("COMP-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.COMP.minimumSize(0.0001))
    // .base(Currency.USD.minimumSize(0.05))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService FTT_USDT = new FTXService("FTT/USDT", MarketSetting.with.spot()
    // .target(Currency.FTT.minimumSize(1))
    // .base(Currency.USDT.minimumSize(0.001))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService YFI_USD = new FTXService("YFI/USD", MarketSetting.with.spot()
    // .target(Currency.YFI.minimumSize(0.001))
    // .base(Currency.USD.minimumSize(5))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService DEFI_PERP = new FTXService("DEFI-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.FTX_DEFI.minimumSize(0.001))
    // .base(Currency.USD.minimumSize(0.1))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService ALT_PERP = new FTXService("ALT-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.FTX_ALT.minimumSize(0.001))
    // .base(Currency.USD.minimumSize(0.01))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService MID_PERP = new FTXService("MID-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.FTX_MID.minimumSize(0.001))
    // .base(Currency.USD.minimumSize(0.1))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService SHIT_PERP = new FTXService("SHIT-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.FTX_SHIT.minimumSize(0.001))
    // .base(Currency.USD.minimumSize(0.1))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService DOGE_PERP = new FTXService("DOGE-PERP",
    // MarketSetting.with.derivative()
    // .target(Currency.DOGE.minimumSize(1))
    // .base(Currency.USD.minimumSize(0.0000005))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // // static final MarketService BTC0625 = new FTXService("BTC-0625",
    // // MarketSetting.with.future()
    // // .target(Currency.BTC.minimumSize(0.0001))
    // // .base(Currency.USD.minimumSize(1))
    // // .acquirableExecutionSize(AcquirableSize));
    //
    // // static final MarketService BTC0924 = new FTXService("BTC-0924",
    // // MarketSetting.with.future()
    // // .target(Currency.BTC.minimumSize(0.0001))
    // // .base(Currency.USD.minimumSize(1))
    // // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService BTC1230 = new FTXService("BTC-1230", MarketSetting.with.future()
    // .target(Currency.BTC.minimumSize(0.0001))
    // .base(Currency.USD.minimumSize(1))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService BTC0331 = new FTXService("BTC-0331", MarketSetting.with.future()
    // .target(Currency.BTC.minimumSize(0.0001))
    // .base(Currency.USD.minimumSize(1))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService ETH1230 = new FTXService("ETH-1230", MarketSetting.with.future()
    // .target(Currency.ETH.minimumSize(0.001))
    // .base(Currency.USD.minimumSize(0.1))
    // .acquirableExecutionSize(AcquirableSize));
    //
    // static final MarketService ETH0331 = new FTXService("ETH-0331", MarketSetting.with.future()
    // .target(Currency.ETH.minimumSize(0.001))
    // .base(Currency.USD.minimumSize(0.1))
    // .acquirableExecutionSize(AcquirableSize));

    // static final MarketService ETH0625 = new FTXService("ETH-0625",
    // MarketSetting.with.future()
    // .target(Currency.ETH.minimumSize(0.001))
    // .base(Currency.USD.minimumSize(0.1))
    // .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public Exchange exchange() {
        return Exchange.FTX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(FTXAccount.class);
    }
}