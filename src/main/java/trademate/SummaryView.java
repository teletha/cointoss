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

import cointoss.MarketService;
import cointoss.MarketType;
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

    private UITableView<MarketService> table;

    private UITableColumn<MarketService, String> name;

    private UITableColumn<MarketService, Num> price;

    private UITableColumn<MarketService, Num> priceForBuy;

    private UITableColumn<MarketService, Num> priceForSell;

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
        size.initialize(Num.ONE).acceptPositiveDecimalInput();

        name.text(CommonText.Market).model(service -> service.formattedId).filterable(true);

        price.text(CommonText.Price)
                .modelBySignal(service -> service.executionLatest()
                        .concat(service.executionsRealtimely().throttle(500, MILLISECONDS))
                        .map(e -> e.price));

        table.query().addQuery(en("Type"), MarketType.class, service -> service.setting.type);

        MarketServiceProvider.availableMarketServices().on(Viewtify.UIThread).to(service -> {
            table.addItemAtLast(service);
        });
    }
}