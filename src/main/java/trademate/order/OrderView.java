/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.order;

import static trademate.CommonText.*;

import java.text.Normalizer.Form;

import javafx.scene.control.SelectionMode;

import cointoss.Direction;
import cointoss.Market;
import cointoss.order.OrderState;
import cointoss.trade.Scenario;
import cointoss.util.arithmetic.Num;
import cointoss.verify.TrainingMarket;
import kiss.Disposable;
import kiss.I;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import stylist.ValueStyle;
import trademate.Theme;
import viewtify.Command;
import viewtify.Key;
import viewtify.Viewtify;
import viewtify.style.FormStyles;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UILabel;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.UIText;
import viewtify.ui.UIVBox;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.User;

public class OrderView extends View {

    /** The active market. */
    public static final Variable<Market> ActiveMarket = Variable.empty();

    /** The current target markert. */
    private Market current;

    /** The market info reset. */
    private Disposable disposer = Disposable.empty();

    /** UI */
    private UIVBox container;

    /** Runner UI */
    private UILabel market;

    /** UI */
    private UIButton takerBuy;

    /** UI */
    private UILabel takerBuyText;

    /** UI */
    private UILabel takerBuyPrice;

    /** UI */
    private UIButton takerSell;

    /** UI */
    private UILabel takerSellText;

    /** UI */
    private UILabel takerSellPrice;

    /** UI */
    private UIButton clear;

    /** UI */
    private UIButton makerBuy;

    /** UI */
    private UILabel makerBuyText;

    /** UI */
    private UILabel makerBuyPrice;

    /** UI */
    private UIButton makerSell;

    /** UI */
    private UILabel makerSellText;

    /** UI */
    private UILabel makerSellPrice;

    /** UI */
    private UIButton cancel;

    /** UI */
    private UIText<Num> orderSize;

    /** UI */
    private UIText<Num> orderThresholdSize;

    /** UI */
    private UICheckBox history;

    /** UI */
    private UICheckBox trainingMode;

    /** UI */
    private UITableView<Scenario> table;

    /** UI */
    private UITableColumn<Scenario, Num> entryPrice;

    /** UI */
    private UITableColumn<Scenario, Num> entrySize;

    /** UI */
    private UITableColumn<Scenario, Num> exitPrice;

    /** UI */
    private UITableColumn<Scenario, Num> exitSize;

    /** UI */
    private UITableColumn<Scenario, Num> profitAndLoss;

    class view extends ViewDSL {
        {
            $(container, FormStyles.FormLabelMin, () -> {
                $(hbox, FormStyles.FormRow, () -> {
                    $(takerSell, FormStyles.FormInputMin);
                    $(clear, FormStyles.FormInputMin);
                    $(takerBuy, FormStyles.FormInputMin);
                });

                $(hbox, FormStyles.FormRow, () -> {
                    $(makerSell, FormStyles.FormInputMin);
                    $(cancel, FormStyles.FormInputMin);
                    $(makerBuy, FormStyles.FormInputMin);
                });

                form(en("Market"), FormStyles.FormInput, market, trainingMode);
                form(Amount, FormStyles.FormInputMin, orderSize, orderThresholdSize, history);

                $(table, style.table, () -> {
                    $(entryPrice, style.Wide);
                    $(entrySize, style.Narrow);
                    $(exitPrice, style.Wide);
                    $(exitSize, style.Narrow);
                    $(profitAndLoss, style.Wide);
                });
            });
        }
    }

    interface style extends StyleDSL {

        Style table = () -> {
            text.unselectable();
        };

        ValueStyle<OrderState> State = state -> {
            switch (state) {
            case REQUESTING:
                $.descendant(() -> {
                    font.color($.rgb(80, 80, 80));
                });
                break;

            default:
                break;
            }
        };

        Style Wide = () -> {
            display.width(100, px);
        };

        Style Narrow = () -> {
            display.width(65, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        ActiveMarket.observing()
                .effectOnce(container::disableNow)
                .skipNull()
                .combineLatest(trainingMode.observing())
                .map(v -> v.ⅱ ? new TrainingMarket(v.ⅰ) : v.ⅰ)
                .effectOnce(container::enableNow)
                .to(m -> {
                    // remove old market info
                    table.removeItemAll();
                    disposer.dispose();

                    // new market
                    current = m;
                    market.text(m);
                    disposer = Disposable.empty();
                    disposer.add(m.trader().observeScenario().to(table::addItemAtLast));
                    disposer.add(m.orderBook.longs.best.observing()
                            .combineLatest(orderSize.observing(), orderThresholdSize.observing())
                            .on(Viewtify.UIThread)
                            .to(v -> {
                                takerSellPrice.text(m.orderBook.longs.predictTakingPrice(v.ⅱ));
                                makerBuyPrice.text(m.orderBook.longs.predictMakingPrice(v.ⅲ));
                            }));
                    disposer.add(m.orderBook.shorts.best.observing()//
                            .combineLatest(orderSize.observing(), orderThresholdSize.observing())
                            .on(Viewtify.UIThread)
                            .to(v -> {
                                takerBuyPrice.text(m.orderBook.shorts.predictTakingPrice(v.ⅱ));
                                makerSellPrice.text(m.orderBook.shorts.predictMakingPrice(v.ⅲ));
                            }));
                });

        Commands.TakeSell.shortcut(Key.Q).contribute(this::takeSelling);
        Commands.Clear.shortcut(Key.W).contribute(this::clear);
        Commands.TakeBuy.shortcut(Key.E).contribute(this::takeBuying);
        Commands.MakeSell.shortcut(Key.A).contribute(this::makeSelling);
        Commands.Cancel.shortcut(Key.S).contribute(this::cancel);
        Commands.MakeBuy.shortcut(Key.D).contribute(this::makeBuying);

        clear.text(en("Clear")).when(User.Action, Commands.Clear);
        takerSell.textV(takerSellText, takerSellPrice).color(Theme.$.sell).when(User.Action, Commands.TakeSell);
        takerSellText.text(en("Take Selling")).color(Theme.$.sell);
        takerBuy.textV(takerBuyText, takerBuyPrice).color(Theme.$.buy).when(User.Action, Commands.TakeBuy);
        takerBuyText.text(en("Take Buying")).color(Theme.$.buy);

        cancel.text(en("Cancel")).when(User.Action, Commands.Cancel);
        makerSell.textV(makerSellText, makerSellPrice).color(Theme.$.sell).when(User.Action, Commands.MakeSell);
        makerSellText.text(en("Make Selling")).color(Theme.$.sell);
        makerBuy.textV(makerBuyText, makerBuyPrice).color(Theme.$.buy).when(User.Action, Commands.MakeBuy);
        makerBuyText.text(en("Make Buying")).color(Theme.$.buy);

        trainingMode.text(en("Demo Trade")).initialize(true);
        orderSize.value(Num.of("0.5")).normalizeInput(Form.NFKC).acceptPositiveNumberInput();
        orderThresholdSize.value(Num.of("3")).normalizeInput(Form.NFKC).acceptPositiveNumberInput();
        history.text(en("Full History"))
                .initialize(false)
                .observing(all -> table.take(all ? Scenario::isNotCancelled : Scenario::isActive));

        initializeTable();
    }

    /**
     * Initialize order table.
     */
    private void initializeTable() {
        // ===============================================
        // Table Part
        // ===============================================
        table.mode(SelectionMode.MULTIPLE).observeItemState(s -> s.state).context(root -> {
            root.menu(en("Clear")).when(User.Action, () -> table.selectedItems().forEach(Scenario::stop));
            root.menu(en("Retreat")).when(User.Action, () -> table.selectedItems().forEach(s -> s.retreat(Num.of(100), Num.of(-100))));
        });

        // ===============================================
        // Entry Part
        // ===============================================
        entryPrice.text(Price)
                .modelBySignal(Scenario::observeEntryPriceNow)
                .render((ui, scenario, price) -> ui.text(price).color(Theme.colorBy(scenario)));
        entrySize.text(Amount).modelBySignal(Scenario::observeEntryExecutedSizeNow).render((ui, scenario, size) -> ui.text(size));

        // ===============================================
        // Exit Part
        // ===============================================
        exitPrice.text(Price).modelBySignal(Scenario::observeExitPriceNow).render((ui, scenario, price) -> ui.text(price));
        exitSize.text(Amount).modelBySignal(Scenario::observeExitExecutedSizeNow).render((ui, scenario, size) -> ui.text(size));

        // ===============================================
        // Analyze Part
        // ===============================================
        profitAndLoss.text(Profit)
                .modelBySignal(scenario -> current.orderBook.by(scenario).best.observing().map(page -> scenario.predictProfit()))
                .render((ui, scenario, profit) -> ui.text(profit).color(Theme.colorBy(profit)));
    }

    private Num estimateSize() {
        if (orderSize.is(Num.ZERO)) {
            return Num.of("0.01");
        } else {
            return orderSize.value();
        }
    }

    private void takeBuying() {
        if (current != null) {
            current.trader().entry(Direction.BUY, estimateSize(), s -> s.take());
        }
    }

    private void takeSelling() {
        if (current != null) {
            current.trader().entry(Direction.SELL, estimateSize(), s -> s.take());
        }
    }

    private void clear() {
        if (current != null) {
            I.signal(table.items()).take(Scenario::isActive).to(Scenario::stop);
        }
    }

    private void makeBuying() {
        if (current != null) {
            current.trader()
                    .entry(Direction.BUY, estimateSize(), s -> s
                            .make(current.orderBook.longs.predictMakingPrice(orderThresholdSize.value())));
        }
    }

    private void makeSelling() {
        if (current != null) {
            current.trader()
                    .entry(Direction.SELL, estimateSize(), s -> s
                            .make(current.orderBook.shorts.predictMakingPrice(orderThresholdSize.value())));
        }
    }

    private void cancel() {
        if (current != null) {
            current.orders.cancelNowAll();
        }
    }

    /**
     * 
     */
    private enum Commands implements Command<Commands> {
        TakeBuy, Clear, TakeSell, MakeBuy, Cancel, MakeSell;
    }
}