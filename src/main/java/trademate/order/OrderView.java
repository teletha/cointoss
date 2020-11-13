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
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.trade.Scenario;
import cointoss.util.arithmetic.Num;
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
import viewtify.ui.UILabel;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.UIText;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.StyleHelper;
import viewtify.ui.helper.User;
import viewtify.ui.helper.Verifier;

public class OrderView extends View {

    /** The active market. */
    public static final Variable<Market> ActiveMarket = Variable.empty();

    /** UI */
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
    private UITableView<Scenario> table;

    /** UI */
    private UITableColumn<Scenario, Direction> side;

    /** UI */
    private UITableColumn<Scenario, Num> amount;

    /** UI */
    private UITableColumn<Scenario, Num> price;

    private UILabel amountTitle;

    private UILabel amountSize;

    class view extends ViewDSL {
        {
            $(vbox, FormStyles.FormLabelMin, () -> {
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

                $(market);
                form(Amount, FormStyles.FormInputMin, orderSize);

                $(table, style.Root, () -> {
                    $(side, style.Narrow);
                    $(price, style.Wide);
                    $(amount, style.Narrow);
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
            display.width(120, px);
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

        takerBuy.text(en("Take Buying")).color(Theme.$.buy).when(User.Action, Commands.TakeBuy);
        clear.text(en("Clear")).when(User.Action, Commands.Clear);
        takerSell.text(en("Take Selling")).color(Theme.$.sell).when(User.Action, Commands.TakeSell);

        makerBuy.text(en("Make Buying")).color(Theme.$.buy).when(User.Action, Commands.MakeBuy);
        cancel.text(en("Cancel")).when(User.Action, Commands.Cancel);
        makerSell.text(en("Make Selling")).color(Theme.$.sell).when(User.Action, Commands.MakeSell);

        orderSize.value("0").normalizeInput(Form.NFKC).acceptPositiveNumberInput().verifyBy(Verifier.PositiveNumber);

        table.mode(SelectionMode.MULTIPLE).render(table -> new CatalogRow()).context($ -> {
            // $.menu().text(Cancel).when(User.Action, e -> act(this::cancel));
        });
        side.text(Side).model(Scenario.class, Scenario::direction).render((label, side) -> label.text(side).color(Theme.colorBy(side)));
        amount.text(amountTitle, amountSize).modelBySignal(o -> o.observeEntrySizeNow());
        amountTitle.text(Amount);
        price.text(Price).modelBySignal(o -> o.observeEntryPriceNow());

        ActiveMarket.observing().skipNull().to(m -> update(m));
    }

    Disposable disposer = Disposable.empty();

    private Disposable update(Market m) {
        disposer.dispose();

        MarketService s = m.service;

        market.text(s.marketReadableName);

        // positionSize.text(m.orders.position).color(position.isPositiveOrZero() ? Theme.$.buy :
        // Theme.$.sell);

        // initialize orders on server
        // m.orders.manages().take(Order::isBuy).sort(Comparator.reverseOrder()).to(this::createOrderItem);
        // I.signal(m.orders.items).take(Order::isSell).sort(Comparator.naturalOrder()).to(this::createOrderItem);

        amountSize.text(m.orders.compoundSize);

        // observe orders on clinet
        return m.trader().scenarios().to(this::createScenarioItem);
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
            m.orders.requestNow(Order.with.buy(estimateSize()));
        });
    }

    private void takeSelling() {
        ActiveMarket.to(m -> {
            m.orders.requestNow(Order.with.sell(estimateSize()));
        });
    }

    private void clear() {
        ActiveMarket.to(m -> {
            Num pos = m.orders.compoundSize.v;
            if (pos.isNotZero()) {
                m.orders.requestNow(Order.with.direction(pos.isPositive() ? Direction.SELL : Direction.BUY, pos.abs()));
            }
        });
    }

    private void makeBuying() {
        ActiveMarket.to(m -> {
            m.trader().when(I.signal(1), v -> new Scenario() {

                @Override
                protected void exit() {
                }

                @Override
                protected void entry() {
                    entry(Direction.BUY, estimateSize(), s -> s.make(m.orderBook.longs.computeBestPrice(Num.HUNDRED, Num.ONE)));
                }
            });
        });
    }

    private void makeSelling() {
        ActiveMarket.to(m -> {
            m.orders.requestNow(Order.with.sell(estimateSize()).price(m.orderBook.shorts.computeBestPrice(Num.HUNDRED, Num.ONE)));
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
