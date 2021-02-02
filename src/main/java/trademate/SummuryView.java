/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import cointoss.Currency;
import cointoss.Market;
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

public class SummuryView extends View {

    private UIScrollPane scroll;

    private UIText<Num> size;

    private UITableView<Market> table;

    private UITableColumn<Market, String> name;

    private UITableColumn<Market, Num> price;

    private UITableColumn<Market, Num> priceForBuy;

    private UITableColumn<Market, Num> priceForSell;

    UIText<String> filter;

    class view extends ViewDSL {
        {
            $(vbox, () -> {
                $(hbox, () -> {
                    $(filter);
                    $(size);
                });
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
            display.height(1000, px);
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
        filter.placeholder("Filter markets").observe().to(this::filterMarkets);

        long throttle = 500;
        size.initialize(Num.ONE).acceptPositiveNumberInput();

        name.text(CommonText.Market).model(m -> m.service.formattedId);

        price.text(CommonText.Price)
                .modelBySignal(m -> m.service.executionLatest()
                        .concat(m.service.executionsRealtimely().throttle(throttle, MILLISECONDS))
                        .map(e -> e.price)
                        .on(Viewtify.UIThread));

        priceForBuy.text(CommonText.Buy)
                .modelBySignal(m -> m.orderBook.shorts.predictTakingPrice(size.observing())
                        .throttle(throttle, MILLISECONDS)
                        .on(Viewtify.UIThread)
                        .retry());

        priceForSell.text(CommonText.Sell)
                .modelBySignal(m -> m.orderBook.longs.predictTakingPrice(size.observing())
                        .throttle(throttle, MILLISECONDS)
                        .on(Viewtify.UIThread)
                        .retry());

        MarketServiceProvider.availableMarketServices().take(service -> service.setting.target.currency == Currency.BTC).to(service -> {
            table.addItemAtLast(Market.of(service));
        });
    }

    /**
     * Filtering markets.
     */
    private void filterMarkets(String text) {
        if (text.isBlank()) {
            table.take(null);
        } else {
            String[] values = text.toLowerCase().split("\\s");

            table.take(m -> {
                String target = m.service.id.toLowerCase();

                for (String value : values) {
                    if (target.contains(value)) {
                        return true;
                    }
                }
                return false;
            });
        }
    }
}
