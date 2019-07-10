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

import static transcript.Transcript.en;

import java.time.Period;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.analyze.Analyzer;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketServiceProvider;
import cointoss.ticker.TickSpan;
import cointoss.trade.Trader;
import cointoss.trade.TradingLog;
import cointoss.util.Chrono;
import cointoss.verify.BackTest;
import trademate.chart.ChartView;
import trademate.setting.SettingStyles;
import transcript.Transcript;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;
import viewtify.ui.View;
import viewtify.ui.helper.User;

public class BackTestView extends View implements Analyzer {

    private static final Transcript LogIsNotFound = Transcript.en("No logs were found for the specified date.");

    private static final Transcript EndDateMustBeAfterStartDate = Transcript.en("The end date must be after the start date.");

    private UIComboBox<MarketService> marketSelection;

    private UIDatePicker startDate;

    private UIDatePicker endDate;

    private UIButton runner;

    private ChartView chart;

    private UIComboBox<TradingLog> logSelection;

    /**
     * UI definition.
     */
    private class UIDefinition extends UI implements SettingStyles {
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
                    $(runner);
                });

                $(vbox, () -> {
                    $(logSelection);
                });
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UIDefinition();
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

        runner.text(en("Run")).disableWhen(startDate.isInvalid()).when(User.MouseClick).on(Viewtify.WorkerThread).to(e -> {
            BackTest.with.service(marketSelection.value())
                    .start(startDate.zoned())
                    .end(endDate.zoned())
                    .exclusiveExecution(false)
                    .runs(market -> {
                        chart.market.set(market);
                        chart.ticker.set(market.tickers.of(TickSpan.Minute5));

                        return List.of(new Sample(market));
                    }, this);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void analyze(List<TradingLog> logs) {
        logSelection.values(logs);
    }

    /**
     * 
     */
    private static class Sample extends Trader {

        private Sample(Market market) {
            super(market);

            when(market.tickers.of(TickSpan.Hour1).add.skip(1), tick -> {
                return new Entry(Direction.random()) {

                    @Override
                    protected void entry() {
                        entry(3, s -> s.make(market.tickers.latest.v.price));
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void exit() {
                        exitAt(entryPrice.plus(direction, 5000));
                        exitAt(entryPrice.minus(direction, 5000));
                        exitAfter(15, TimeUnit.MINUTES);
                    }
                };
            });
        }
    }
}
