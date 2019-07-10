/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static trademate.setting.SettingStyles.*;
import static transcript.Transcript.en;

import java.time.Period;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketServiceProvider;
import cointoss.ticker.TickSpan;
import cointoss.util.Chrono;
import trademate.chart.ChartView;
import transcript.Transcript;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;
import viewtify.ui.View;
import viewtify.ui.helper.User;

public class BackTestView extends View {

    private static final Transcript LogIsNotFound = Transcript.en("No logs were found for the specified date.");

    private static final Transcript EndDateMustBeAfterStartDate = Transcript.en("The end date must be after the start date.");

    private UIComboBox<MarketService> marketSelection;

    private UIDatePicker startDate;

    private UIDatePicker endDate;

    private UIButton startButton;

    private ChartView chart;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(hbox, () -> {
                    $(chart);
                    $(vbox, () -> {
                        $(marketSelection);
                        $(hbox, FormRow, () -> {
                            label(en("Start Date"), FormLabel);
                            $(startDate, FormInput);
                        });
                        $(hbox, FormRow, () -> {
                            label(en("End Date"), FormLabel);
                            $(endDate, FormInput);
                        });
                        $(startButton);
                    });
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        marketSelection.values(0, MarketServiceProvider.availableMarketServices());
        startDate.initial(Chrono.utcNow().minusDays(10)).uneditable().requireWhen(marketSelection).require(() -> {
            ExecutionLog log = marketSelection.value().log;

            assert startDate.isBeforeOrSame(log.lastCacheDate()) : LogIsNotFound;
            assert startDate.isAfterOrSame(log.firstCacheDate()) : LogIsNotFound;
        }).observe((o, n) -> {
            endDate.value(v -> v.plus(Period.between(o, n)));
        });

        endDate.initial(Chrono.utcNow()).uneditable().require(() -> {
            assert startDate.isBeforeOrSame(endDate) : EndDateMustBeAfterStartDate;
        });

        // Market market = new Market(BitFlyer.BTC_JPY).readLog(log -> log.at(2018, 1, 17));

        // Viewtify.Terminator.add(market);
        // chart.market.set(market);
        // chart.ticker.set(market.tickers.tickerBy(TickSpan.Minute1));

        startButton.text(en("Run")).disableWhen(startDate.isInvalid()).when(User.MouseClick).to(e -> {
            Market market = new Market(marketSelection.value());
            chart.market.set(market);
            chart.ticker.set(market.tickers.of(TickSpan.Minute1));
            chart.market.to(v -> v.readLog(log -> log.range(startDate.zoned(), endDate.zoned())));

            Viewtify.Terminator.add(market);
        });
    }
}
