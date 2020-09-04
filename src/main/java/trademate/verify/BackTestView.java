/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.verify;

import java.time.Period;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.analyze.Analyzer;
import cointoss.analyze.NumStats;
import cointoss.analyze.TradingStats;
import cointoss.execution.ExecutionLog;
import cointoss.execution.ExecutionLog.LogType;
import cointoss.market.MarketServiceProvider;
import cointoss.trade.Trader;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import cointoss.verify.BackTest;
import kiss.Disposable;
import kiss.I;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.TradeMate;
import trademate.chart.ChartView;
import trademate.chart.PlotScript;
import trademate.chart.builtin.TraderVisualizer;
import trademate.setting.SettingStyles;
import viewtify.Viewtify;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;
import viewtify.ui.UILabel;
import viewtify.ui.UIPane;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.User;

public class BackTestView extends View implements Analyzer {

    /** Runner UI */
    private UIComboBox<MarketService> marketSelection;

    /** Runner UI */
    private UIDatePicker startDate;

    /** Runner UI */
    private UIDatePicker endDate;

    /** Runner UI */
    private UIButton runner;

    /** Runner UI */
    private UICheckBox fastLog;

    private ChartView chart;

    /** UI */
    private UITableView results;

    /** UI */
    private UITableColumn<TradingStats, TradingStats> name;

    /** The trading statistics. */
    private UITableColumn<TradingStats, TradingStats> period;

    /** The trading statistics. */
    private UITableColumn<TradingStats, TradingStats> holdSize;

    /** The trading statistics. */
    private UITableColumn<TradingStats, NumStats> holdTimeForProfit;

    /** The trading statistics. */
    private UITableColumn<TradingStats, NumStats> holdTimeForLoss;

    /** The trading statistics. */
    private UITableColumn<TradingStats, TradingStats> realizedProfit;

    /** The trading statistics. */
    private UITableColumn<TradingStats, TradingStats> realizedLoss;

    /** The trading statistics. */
    private UITableColumn<TradingStats, NumStats> profit;

    /** The trading statistics. */
    private UITableColumn<TradingStats, String> total;

    /** The trading statistics. */
    private UITableColumn<TradingStats, Num> winRatio;

    /** The trading statistics. */
    private UITableColumn<TradingStats, Num> profitFactor;

    /** The trading statistics. */
    private UITableColumn<TradingStats, Num> drawDown;

    /** The trading statistics. */
    private UITableColumn<TradingStats, TradingStats> scenarioCount;

    /** The last disposer. */
    private Disposable lastDisposer = Disposable.empty();

    /** The verification state. */
    private Variable<Boolean> verifying = Variable.of(false);

    /** The trader builder. */
    private final List<BotEditor> builders = I.find(Trader.class).stream().map(trader -> {
        trader.disable(); // default

        BotEditor editor = new BotEditor(trader);
        editor.rejectableProperty.set(p -> p.name.equals("profit") || p.name.equals("holdMaxSize") || p.name.equals("holdSize"));
        return editor;
    }).collect(Collectors.toList());

    /** Runner UI */
    private UIComboBox<BotEditor> botSelector;

    /** Runner UI */
    private UIPane botEditor;

    /**
     * UI definition.
     */
    class view extends ViewDSL implements SettingStyles {
        {
            $(vbox, () -> {
                $(hbox, style.fill, () -> {
                    $(chart);
                    $(vbox, style.config, () -> {
                        $(hbox, style.formRow, () -> {
                            label(en("Market"), style.formLabel);
                            $(marketSelection);
                        });
                        $(hbox, style.formRow, () -> {
                            label(en("Start"), style.formLabel);
                            $(startDate, style.formInput);
                            label(en("End"), style.formLabel);
                            $(endDate, style.formInput);
                        });
                        $(hbox, style.formRow, () -> {
                            $(runner);
                            $(fastLog, style.formCheck);
                        });

                        $(botSelector, style.bot);
                        $(botEditor);
                    });
                });

                $(hbox, () -> {
                    $(results, style.testResult, () -> {
                        $(name);
                        $(period);
                        $(holdSize, style.shortColumn);
                        $(scenarioCount, style.shortColumn);
                        $(holdTimeForProfit, style.shortColumn);
                        $(holdTimeForLoss, style.shortColumn);
                        $(realizedProfit, style.priceColumn);
                        $(realizedLoss, style.priceColumn);
                        $(profit);
                        $(total);
                        $(winRatio);
                        $(profitFactor);
                        $(drawDown);
                    });
                });
            });
        }
    }

    /**
     * Style definition
     */
    interface style extends StyleDSL {
        Style bot = () -> {
            margin.top(10, px);
        };

        Style fill = () -> {
            display.height.fill().width.fill();
        };

        Style config = () -> {
            display.minWidth(400, px).maxWidth(400, px);
        };

        Style testResult = () -> {
            display.minHeight(330, px);
        };

        Style formRow = () -> {
            display.minHeight(30, px);
            padding.vertical(3, px);
            text.verticalAlign.middle();
        };

        Style formLabel = () -> {
            display.minWidth(40, px);
            padding.top(3, px);
        };

        Style formInput = () -> {
            display.minWidth(110, px);
            margin.right(20, px);
        };

        Style formCheck = () -> {
            display.minWidth(60, px);
            padding.left(10, px);
        };

        Style traderSelect = () -> {
            margin.top(12, px).bottom(8, px);
        };

        Style max = () -> {
            font.color(255, 230, 230);
        };

        Style mean = () -> {
            font.color(205, 230, 250);
        };

        Style priceColumn = () -> {
            display.width(105, px).minWidth(105, px);
        };

        Style shortColumn = () -> {
            display.width(65, px).minWidth(65, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        marketSelection.initialize(MarketServiceProvider.availableMarketServices())
                .render(service -> service.marketReadableName)
                .renderSelected(service -> service.marketReadableName);

        fastLog.initialize(false)
                .text(en("Use Fast Log"))
                .tooltip(en("Run backtests very fast using compressed execution history.\nHowever, the execution result may be inaccurate."));
        startDate.initial(Chrono.utcNow().minusDays(10))
                .uneditable()
                .observe((o, n) -> endDate.value(v -> v.plus(Period.between(o, n))))
                .verifyWhen(marketSelection.isChanged())
                .verify(en("No logs were found for the specified date."), v -> {
                    ExecutionLog log = marketSelection.value().log;
                    return startDate.isBeforeOrSame(log.lastCacheDate()) && startDate.isAfterOrSame(log.firstCacheDate());
                });

        endDate.initial(Chrono.utcNow())
                .uneditable()
                .verify(en("The end date must be after the start date."), v -> startDate.isBeforeOrSame(endDate.value()));

        runner.text(en("Run"))
                .disableWhen(startDate.isInvalid(), endDate.isInvalid(), verifying.observing())
                .when(User.MouseClick)
                .on(Viewtify.WorkerThread)
                .to(e -> {
                    lastDisposer.dispose();

                    BackTest.with.service(marketSelection.value())
                            .start(startDate.zoned())
                            .end(endDate.zoned())
                            .traders(I.signal(builders).flatIterable(b -> b.build()).take(Trader::isEnable).toList())
                            .initialBaseCurrency(3000000)
                            .type(fastLog.value() ? LogType.Fast : LogType.Normal)
                            .run(this);
                });

        botSelector.items(builders)
                .selectFirst()
                .render(b -> b.trader.name())
                .renderSelected(b -> b.trader.name())
                .whenSelected(botEditor::content);

        configureTradingLogView();
        I.make(TradeMate.class).requestLazyInitialization();
    }

    /**
     * Setting for trading log.
     */
    private void configureTradingLogView() {
        name.text(en("Name")).model(log -> log).render(this::renderName);
        period.text(en("Period")).model(log -> log).render(this::renderPeriod);
        holdSize.text(en("Hold Size")).model(log -> log).render(this::renderPositionSize);
        holdTimeForProfit.text(en("Profit Span")).model(log -> log.holdTimeOnProfitTrade).render(this::render);
        holdTimeForLoss.text(en("Loss Span")).model(log -> log.holdTimeOnLossTrade).render(this::render);
        realizedProfit.text(en("Realized Profit"))
                .model(log -> log)
                .render((ui, log) -> render(ui, log.profitRange, log.unrealizedProfitRange));
        realizedLoss.text(en("Realized Loss")).model(log -> log).render((ui, log) -> render(ui, log.lossRange, log.unrealizedLossRange));
        profit.text(en("Profit")).model(log -> log.profitAndLoss).render(this::render);
        total.text(en("Total Profit")).model(log -> log.profitAndLoss.formattedTotal());
        winRatio.text(en("Win Rate")).model(log -> log.winningRate());
        profitFactor.text(en("Profit Factor")).model(TradingStats::profitFactor);
        drawDown.text(en("Drawdown")).model(log -> log.drawDownRatio);
        scenarioCount.text(en("Trade Count")).model(log -> log).render(this::renderScenarioCount);
    }

    /**
     * Render scenario count.
     * 
     * @param cell
     * @param log
     */
    private void renderName(UILabel label, TradingStats log) {
        label.text(log.name)
                .tooltip(log.properties.entrySet()
                        .stream()
                        .map(e -> e.getKey() + " : " + e.getValue())
                        .collect(Collectors.joining("\r\n")));
    }

    /**
     * Render scenario count.
     * 
     * @param cell
     * @param log
     */
    private void renderPeriod(UILabel label, TradingStats log) {
        UILabel remaining = new UILabel(this).tooltip(en("Start")).text(Chrono.formatAsDate(log.startDate)).style(style.mean);
        UILabel total = new UILabel(this).tooltip(en("End")).text(Chrono.formatAsDate(log.endDate)).style(style.mean);

        label.textV(remaining, total);
    }

    /**
     * Render scenario count.
     * 
     * @param cell
     * @param log
     */
    private void renderScenarioCount(UILabel label, TradingStats log) {
        UILabel remaining = new UILabel(this).tooltip(en("Remaining")).text(log.active).style(style.mean);
        UILabel total = new UILabel(this).tooltip(en("Total")).text(log.total).style(style.max);

        label.textV(remaining, total);
    }

    /**
     * Render position size.
     * 
     * @param cell
     * @param log
     */
    private void renderPositionSize(UILabel label, TradingStats log) {
        int target = marketSelection.value().setting.target.scale;

        UILabel mean = new UILabel(this).tooltip(en("Remaining")).text(log.holdCurrentSize.scale(target)).style(style.mean);
        UILabel max = new UILabel(this).tooltip(en("Max")).text(log.holdMaxSize.scale(target)).style(style.max);

        label.textV(mean, max);
    }

    /**
     * Render {@link NumStats}.
     * 
     * @param cell
     * @param statistics
     */
    private void render(UILabel label, NumStats statistics) {
        UILabel mean = new UILabel(this).tooltip(en("Mean")).text(statistics.formattedMean()).style(style.mean);
        UILabel max = new UILabel(this).tooltip(en("Max")).text(statistics.formattedMax()).style(style.max);
        UILabel min = new UILabel(this).tooltip(en("Min")).text(statistics.formattedMin()).style(style.max);

        label.textV(mean, max, min);
    }

    /**
     * Render {@link NumStats}.
     * 
     * @param cell
     * @param main
     */
    private void render(UILabel label, NumStats main, NumStats sub) {
        UILabel mean = new UILabel(this).tooltip(en("Mean"))
                .text(main.formattedMean() + " (" + sub.formattedMean() + ")")
                .style(style.mean);
        UILabel max = new UILabel(this).tooltip(en("Max")).text(main.formattedMax() + " (" + sub.formattedMax() + ")").style(style.max);
        UILabel min = new UILabel(this).tooltip(en("Min")).text(main.formattedMin() + " (" + sub.formattedMin() + ")").style(style.max);

        label.textV(mean, max, min);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Market market, List<Trader> traders) {
        verifying.set(true);

        chart.showRealtimeUpdate.set(false);
        chart.showOrderSupport.set(false);
        chart.showOrderbook.value(false);
        chart.showLatestPrice.value(false);
        chart.market.set(market);
        chart.scripts.clear();
        chart.scripts.addAll(I.signal(traders)
                .flatIterable(t -> t.findOptionBy(PlotScript.class))
                .map(e -> (Supplier<PlotScript>) () -> e)
                .startWith(TraderVisualizer::new)
                .toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void analyze(Market market, List<TradingStats> logs, boolean detail) {
        for (TradingStats log : logs) {
            System.out.println(log);
            results.addItemAtFirst(log);
        }

        verifying.set(false);
        chart.chart.layoutForcely();
        lastDisposer = market::dispose;
    }
}