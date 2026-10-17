/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static java.util.concurrent.TimeUnit.*;

import javafx.scene.control.TableRow;

import cointoss.MarketService;
import cointoss.MarketType;
import cointoss.market.MarketServiceProvider;
import hypatia.Num;
import kiss.I;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.Viewtify;
import viewtify.ui.UIScrollPane;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.UIText;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.User;
import viewtify.ui.helper.UserActionHelper;
import viewtify.ui.query.CompoundQuery;

public class SummaryView extends View {

    private UIScrollPane scroll;

    private UIText<String> filter;

    private UITableView<MarketService> table;

    private UITableColumn<MarketService, String> market;

    private UITableColumn<MarketService, String> pair;

    private UITableColumn<MarketService, Num> price;

    class view extends ViewDSL {
        {
            $(vbox, () -> {
                $(filter);
                $(table, style.table, () -> {
                    $(market, style.market);
                    $(pair, style.pair);
                    $(price, style.price);
                });
            });
        }
    }

    interface style extends StyleDSL {
        Style table = () -> {
            display.height.fill();
        };

        Style market = () -> {
            display.width(100, px);
            text.align.left();
        };

        Style pair = () -> {
            display.width(140, px);
            text.align.left();
        };

        Style price = () -> {
            display.width(90, px);
            text.align.right();
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        filter.placeholder(en("Filter")).value("");

        market.text(CommonText.Market).model(service -> service.exchange.name()).filterable(true);

        pair.text(en("Currency Pair")).model(service -> service.marketName).filterable(true);

        price.text(CommonText.Price)
                .modelBySignal(service -> service.executionLatest()
                        .concat(service.executionsRealtimely().throttle(500, MILLISECONDS))
                        .map(e -> e.price()));

        // Filter the table by the text on the top.
        CompoundQuery.Query<MarketService, String> condition = table.query()
                .addQuery(en("Filter"), String.class, service -> service.exchange.name() + " " + service.marketName);
        condition.tester.set(CompoundQuery.Tester.Contain);
        condition.input.set("");
        filter.sync(condition.input);

        table.query().addQuery(en("Type"), MarketType.class, service -> service.setting.type());

        // Open the chart of the double-clicked market.
        table.render(t -> {
            TableRow<MarketService> row = new TableRow();
            UserActionHelper.of(row).when(User.DoubleClick, () -> {
                MarketService service = row.getItem();
                if (service != null) {
                    I.make(TradeMateDockProvider.class).trade.show(service);
                }
            });
            return row;
        });

        MarketServiceProvider.availableMarketServices().on(Viewtify.UIThread).to(service -> {
            table.addItemAtLast(service);
        });
    }
}