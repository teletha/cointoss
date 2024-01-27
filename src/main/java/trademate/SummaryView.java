/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static java.util.concurrent.TimeUnit.*;

import cointoss.Market;
import cointoss.MarketType;
import cointoss.market.Exchange;
import cointoss.market.MarketServiceProvider;
import cointoss.util.arithmetic.Num;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.Viewtify;
import viewtify.ui.UIScrollPane;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.UIText;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

public class SummaryView extends View {

    private UIScrollPane scroll;

    private UIText<Num> size;

    private UITableView<Market> table;

    private UITableColumn<Market, String> name;

    private UITableColumn<Market, Num> price;

    private UITableColumn<Market, Num> priceForBuy;

    private UITableColumn<Market, Num> priceForSell;

    class view extends ViewDSL {
        {
            $(vbox, () -> {
                $(size);
                $(table, style.table, () -> {
                    $(name, style.name);
                    $(price, style.normal);
                    $(priceForBuy, style.normal);
                    $(priceForSell, style.normal);
                });
            });
        }
    }

    interface style extends StyleDSL {
        Style table = () -> {
            display.height.fill();
        };

        Style name = () -> {
            display.width(160, px);
            text.align.left();
        };

        Style normal = () -> {
            display.width(90, px);
            text.align.right();
        };

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        long throttle = 500;
        size.initialize(Num.ONE).acceptPositiveDecimalInput();

        name.text(CommonText.Market).model(m -> m.service.formattedId).filterable(true);

        price.text(CommonText.Price)
                .modelBySignal(m -> m.service.executionLatest()
                        .concat(m.service.executionsRealtimely().throttle(throttle, MILLISECONDS))
                        .map(e -> e.price)
                        .on(Viewtify.UIThread));

        table.query().addQuery(en("Type"), MarketType.class, m -> m.service.setting.type);

        MarketServiceProvider.availableMarketServices()
                .skip(x -> x.exchange == Exchange.Bybit)
                .skip(x -> x.exchange == Exchange.BitBank)
                // .skip(x -> x.exchange == Exchange.Binance)
                .skip(x -> x.exchange == Exchange.BinanceF)
                .skip(x -> x.exchange == Exchange.Bitfinex)
                .skip(x -> x.exchange == Exchange.BitFlyer)
                .skip(x -> x.exchange == Exchange.BitMEX)
                .skip(x -> x.exchange == Exchange.Coinbase)
                .skip(x -> x.exchange == Exchange.FTX)
                .skip(x -> x.exchange == Exchange.Huobi)
                .skip(x -> x.exchange == Exchange.GMO)
                .on(Viewtify.WorkerThread)
                .map(Market::of)
                .to(x -> {
                    System.out.println(x);
                    table.addItemAtLast(x);
                });
    }
}