/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.verify;

import static transcript.Transcript.en;

import java.time.Period;
import java.util.List;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.TradingLog;
import cointoss.analyze.Analyzer;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketServiceProvider;
import cointoss.trading.LazyBear;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.verify.BackTest;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartView;
import trademate.setting.SettingStyles;
import transcript.Transcript;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
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

    /** UI */
    private UITableView results;

    /** UI */
    private UITableColumn<TradingLog, String> name;

    /** UI */
    private UITableColumn<TradingLog, Num> profitAndLoss;

    /** UI */
    private UITableColumn<TradingLog, Num> winRatio;

    /** UI */
    private UITableColumn<TradingLog, Num> riskRewardRatio;

    /**
     * UI definition.
     */
    class view extends UI implements SettingStyles {

        {
            $(vbox, () -> {
                $(hbox, () -> {
                    $(chart);
                    $(vbox, () -> {
                        $(marketSelection);
                        $(hbox, FormRow, () -> {
                            label(en("Start"), style.formLabel);
                            $(startDate, style.FormInput);
                            label(en("End"), style.formLabel);
                            $(endDate, style.FormInput);
                        });
                        $(runner);
                    });
                });

                $(hbox, () -> {
                    $(results, style.testResult, () -> {
                        $(name);
                        $(profitAndLoss);
                        $(winRatio);
                        $(riskRewardRatio);
                    });
                });
            });
        }
    }

    /**
     * Style definition
     */
    interface style extends StyleDSL {
        Style testResult = () -> {
            display.width(300, px).maxHeight(150, px).minHeight(150, px);
        };

        Style formLabel = () -> {
            display.minWidth(40, px);
            padding.top(3, px);
        };

        Style FormInput = () -> {
            display.minWidth(110, px);
            margin.right(20, px);
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

        runner.text(en("Run")).disableWhen(startDate.isInvalid()).when(User.MouseClick).on(Viewtify.WorkerThread).to(e -> {
            BackTest.with.service(marketSelection.value())
                    .start(startDate.zoned())
                    .end(endDate.zoned())
                    .traders(LazyBear::new)
                    .initialBaseCurrency(3000000)
                    .run(this);
        }, e -> {
            e.printStackTrace();
        });

        configureTradingLogView();
    }

    /**
     * Setting for trading log.
     */
    private void configureTradingLogView() {
        name.header(en("Name")).model(log -> log.trader.name());
        profitAndLoss.header(en("Profit")).model(log -> log.profitAndLoss.total());
        winRatio.header(en("Win Rate")).model(log -> log.winningRate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Market market) {
        chart.showOrderSupport.set(false);
        chart.showPositionSupport.set(false);
        chart.showLatestPrice.set(false);
        chart.showRealtimeUpdate.set(false);

        chart.market.set(market);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void analyze(Market market, List<TradingLog> logs, boolean detail) {
        for (TradingLog log : logs) {
            System.out.println(log);
            results.values.add(log);
        }

        Viewtify.inUI(() -> {
            logSelection.values(logs);

            market.dispose();
        });
    }
}
