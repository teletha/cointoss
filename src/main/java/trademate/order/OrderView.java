/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.order;

import static trademate.CommonText.*;

import java.text.Normalizer.Form;

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.Execution;
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
import viewtify.ui.helper.StyleHelper;
import viewtify.ui.helper.User;
import viewtify.ui.helper.Verifier;

public class OrderView extends View {

    /** The active market. */
    public static final Variable<Market> ActiveMarket = Variable.empty();

    /** UI */
    private UIVBox rootP;

    /** Runner UI */
    private UILabel market;

    /** UI */
    private UIButton takerBuy;

    /** UI */
    private UIButton takerSell;

    /** UI */
    private UIButton clear;

    /** UI */
    private UIButton makerBuy;

    /** UI */
    private UIButton makerSell;

    /** UI */
    private UIButton cancel;

    /** UI */
    private UIText orderSize;

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
            $(rootP, FormStyles.FormLabelMin, () -> {
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
                form(Amount, FormStyles.FormInputMin, orderSize);

                $(table, style.Root, () -> {
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

        Style Root = () -> {
            display.width(400, px).minHeight(300, px);
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
        Commands.TakeSell.shortcut(Key.Q).contribute(this::takeSelling);
        Commands.Clear.shortcut(Key.W).contribute(this::clear);
        Commands.TakeBuy.shortcut(Key.E).contribute(this::takeBuying);
        Commands.MakeSell.shortcut(Key.A).contribute(this::makeSelling);
        Commands.Cancel.shortcut(Key.S).contribute(this::cancel);
        Commands.MakeBuy.shortcut(Key.D).contribute(this::makeBuying);

        rootP.disableWhen(ActiveMarket, m -> m == null);
        market.text(ActiveMarket);

        takerBuy.text(en("Take Buying")).color(Theme.$.buy).when(User.Action, Commands.TakeBuy);
        clear.text(en("Clear")).when(User.Action, Commands.Clear);
        takerSell.text(en("Take Selling")).color(Theme.$.sell).when(User.Action, Commands.TakeSell);

        makerBuy.text(en("Make Buying")).color(Theme.$.buy).when(User.Action, Commands.MakeBuy);
        cancel.text(en("Cancel")).when(User.Action, Commands.Cancel);
        makerSell.text(en("Make Selling")).color(Theme.$.sell).when(User.Action, Commands.MakeSell);

        trainingMode.text(en("Use demo trade")).initialize(true).when(User.Action, v -> {
            ActiveMarket.set(m -> {
                if (m instanceof TrainingMarket) {
                    return trainingMode.value() ? m : ((TrainingMarket) m).backend;
                } else {
                    return trainingMode.value() ? new TrainingMarket(m) : m;
                }
            });
        });
        orderSize.value("0.5").normalizeInput(Form.NFKC).acceptPositiveNumberInput().verifyBy(Verifier.PositiveNumber);

        initializeTable();

        ActiveMarket.observing().skipNull().to(m -> {
            update(m);
            if (m instanceof TrainingMarket == false) {
                trainingMode.value(false);
            }
        });
    }

    private void initializeTable() {
        // ===============================================
        // Table Part
        // ===============================================
        table.mode(SelectionMode.MULTIPLE).context(root -> {
            root.menu(en("Clear")).when(User.Action, () -> table.selectedItems().forEach(Scenario::stop));
            root.menu(en("Retreat")).when(User.Action, () -> table.selectedItems().forEach(Scenario::stopRetreat));
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
                .modelBySignal(s -> ActiveMarket.observing().flatVariable(m -> m.tickers.latest).map(Execution::price).map(s::profit))
                .render((ui, profit) -> ui.text(profit).color(Theme.colorBy(profit)));
    }

    Disposable disposer = Disposable.empty();

    private Disposable update(Market m) {
        disposer.dispose();

        MarketService s = m.service;

        // positionSize.text(m.orders.position).color(position.isPositiveOrZero() ? Theme.$.buy :
        // Theme.$.sell);

        // initialize orders on server
        // m.orders.manages().take(Order::isBuy).sort(Comparator.reverseOrder()).to(this::createOrderItem);
        // I.signal(m.orders.items).take(Order::isSell).sort(Comparator.naturalOrder()).to(this::createOrderItem);

        // observe orders on clinet
        return I.signal(m.trader().scenarios()).merge(m.trader().added).to(this::createScenarioItem);
    }

    /**
     * Create tree item for {@link OrderSet}.
     * 
     * @param set
     */
    private void createScenarioItem(Scenario order) {
        if (order != null) {
            table.addItemAtLast(order);
        }
    }

    private Num estimateSize() {
        if (orderSize.is("0")) {
            return Num.of("0.01");
        } else {
            return Num.of(orderSize.value());
        }
    }

    private void takeBuying() {
        ActiveMarket.to(m -> {
            m.trader().entry(Direction.BUY, estimateSize(), s -> s.take());
        });
    }

    private void takeSelling() {
        ActiveMarket.to(m -> {
            m.trader().entry(Direction.SELL, estimateSize(), s -> s.take());
        });
    }

    private void clear() {
        ActiveMarket.to(m -> {
            I.signal(table.items()).take(Scenario::isActive).to(Scenario::stop);
        });
    }

    private void makeBuying() {
        ActiveMarket.to(m -> {
            m.trader().entry(Direction.BUY, estimateSize(), s -> s.make(m.orderBook.longs.computeBestPrice(Num.ONE, Num.ONE)));
        });
    }

    private void makeSelling() {
        ActiveMarket.to(m -> {
            m.trader().entry(Direction.SELL, estimateSize(), s -> s.make(m.orderBook.shorts.computeBestPrice(Num.ONE, Num.ONE)));
        });
    }

    private void cancel() {
        ActiveMarket.to(m -> {
            m.orders.cancelNowAll();
        });
    }

    /**
     * 
     */
    private enum Commands implements Command<Commands> {
        TakeBuy, Clear, TakeSell, MakeBuy, Cancel, MakeSell;
    }

    /**
     * 
     */
    private class CatalogRow extends TableRow<Scenario> implements StyleHelper<CatalogRow, CatalogRow> {

        /**
         * 
         */
        private CatalogRow() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CatalogRow ui() {
            return this;
        }
    }
}
