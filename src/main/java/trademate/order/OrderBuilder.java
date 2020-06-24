/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.order;

import static cointoss.order.OrderState.*;
import static trademate.CommonText.*;

import java.math.RoundingMode;
import java.text.Normalizer.Form;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.input.ScrollEvent;

import cointoss.Direction;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitflyer.SFD;
import cointoss.order.Order;
import cointoss.order.OrderManager;
import cointoss.order.OrderState;
import cointoss.trading.LazyBear;
import cointoss.util.Num;
import kiss.I;
import kiss.WiseBiConsumer;
import kiss.WiseConsumer;
import stylist.Style;
import stylist.StyleDSL;
import stylist.ValueStyle;
import trademate.TradeMateStyle;
import trademate.TradingView;
import viewtify.Viewtify;
import viewtify.bind.Calculated;
import viewtify.style.FormStyles;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UILabel;
import viewtify.ui.UIScrollPane;
import viewtify.ui.UISpinner;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.UIText;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.StyleHelper;
import viewtify.ui.helper.User;
import viewtify.ui.helper.ValueHelper;
import viewtify.ui.helper.Verifier;

public class OrderBuilder extends View {

    private WiseConsumer<ValueHelper> positiveNumber = ui -> {
        if (Num.of(String.valueOf(ui.value())).isPositive() == false) {
            throw new NumberFormatException(ui.value() + " is not positive number.");
        }
    };

    /** UI */
    private UIText orderSize;

    /** UI */
    private UISpinner<Num> orderSizeAmount;

    /** UI */
    UIText orderPrice;

    /** UI */
    private UISpinner<Num> orderPriceAmount;

    /** UI */
    private UISpinner<Integer> orderDivideSize;

    /** UI */
    private UISpinner<Integer> orderDivideIntervalAmount;

    /** UI */
    private UISpinner<Num> optimizeThreshold;

    /** UI */
    private UIText orderPriceInterval;

    /** UI */
    private UISpinner<Num> orderPriceIntervalAmount;

    /** UI */
    private UIButton orderLimitLong;

    /** UI */
    private UIButton orderLimitShort;

    /** UI */
    private UIButton orderCancel;

    /** UI */
    private UIButton orderStop;

    /** UI */
    private UIButton orderReverse;

    /** UI */
    private TradingView view;

    /** UI */
    private UILabel positionSize;

    /** UI */
    private UILabel sfdPrice500;

    /** UI */
    private UILabel sfdPrice499;

    private UICheckBox bot;

    /** UI */
    private UITableView<Order> table;

    /** UI */
    private UITableColumn<Order, Direction> side;

    /** UI */
    private UITableColumn<Order, Num> amount;

    /** UI */
    private UITableColumn<Order, Num> price;

    private UIScrollPane scroll;

    class View extends ViewDSL implements TradeMateStyle, FormStyles {
        {
            $(OrderBuilder.this.scroll, () -> {
                $(vbox, S.Root, FormLabelMin, () -> {
                    form(Amount, FormInputMin, orderSize, orderSizeAmount);
                    form(Price, FormInputMin, orderPrice, orderPriceAmount);
                    form(en("Variances"), FormInputMin, orderDivideSize, orderDivideIntervalAmount);
                    form(en("Price Interval"), FormInputMin, orderPriceInterval, orderPriceIntervalAmount);
                    form(en("Threshold"), FormInputMin, optimizeThreshold);
                    form(FormButton, orderLimitShort.style(Short), orderLimitLong.style(Long));
                    form(FormButton, orderCancel, orderStop, orderReverse);
                    form(en("Position"), FormInputMin, positionSize);

                    $(table, S.Catalog, () -> {
                        $(side, S.Narrow);
                        $(price, S.Wide);
                        $(amount, S.Narrow);
                    });
                });
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        scroll.policy(ScrollBarPolicy.NEVER, ScrollBarPolicy.AS_NEEDED);

        OrderManager orders = view.market.orders;

        orderSize.initialize("0")
                .normalizeInput(Form.NFKC)
                .acceptPositiveNumberInput()
                .verifyBy(Verifier.PositiveNumber)
                .when(User.Scroll, changeBy(orderSizeAmount));
        orderSizeAmount.initialize(view.service.setting.targetCurrencyBidSizes());

        orderPrice.initialize("0")
                .normalizeInput(Form.NFKC)
                .acceptPositiveNumberInput()
                .when(User.Scroll, changeBy(orderPriceAmount))
                .when(User.MiddleClick, () -> {
                    orderPrice.value(view.market.tickers.latest.v.price.toString());
                });
        orderPriceAmount.initialize(Num.ONE, Num.HUNDRED, Num.THOUSAND, Num.of(10000));

        orderDivideIntervalAmount.initialize(IntStream.rangeClosed(0, 6));
        orderDivideSize.initialize(IntStream.rangeClosed(1, 12)).observing(v -> {
            boolean disable = v == 1;
            orderDivideIntervalAmount.disable(disable);
            orderPriceInterval.disable(disable);
            orderPriceIntervalAmount.disable(disable);
        });

        orderPriceIntervalAmount.initialize(Num.TEN, Num.HUNDRED, Num.THOUSAND);
        orderPriceInterval.initialize("0")
                .normalizeInput(Form.NFKC)
                .acceptPositiveNumberInput()
                .when(User.Scroll, changeBy(orderPriceAmount))
                .when(User.Scroll, changeBy(orderPriceIntervalAmount));

        optimizeThreshold.initialize(Num.range(0, 200));

        // validate order condition
        orderLimitLong.parent().disableWhen(orderSize.isInvalid(), orderPrice.isInvalid());

        orderLimitLong.text(Buy).when(User.LeftClick, (e, ui) -> {
            requestOrder(Direction.BUY);
            ui.disableBriefly();
        });
        orderLimitShort.text(Sell).when(User.LeftClick, (e, ui) -> {
            requestOrder(Direction.SELL);
            ui.disableBriefly();
        });

        orderCancel.text(en("Cancel")).when(User.MouseClick).on(Viewtify.WorkerThread).to(() -> orders.cancelNowAll());
        orderStop.text(en("Stop")).when(User.MouseClick).on(Viewtify.WorkerThread).to(() -> view.market.stop().to(I.NoOP));
        orderReverse.text(en("Reverse")).when(User.MouseClick).on(Viewtify.WorkerThread).to(() -> view.market.reverse().to(I.NoOP));

        if (view.market.service == BitFlyer.FX_BTC_JPY) {
            view.market.service.add(SFD.now().on(Viewtify.UIThread).to(price -> {
                sfdPrice500.text("-5.00% " + price.ⅱ.multiply(0.95).scale(0));
            }));
        }

        view.market.orders.position.observing().on(Viewtify.UIThread).to(position -> {
            positionSize.text(position).styleOnly(position.isPositiveOrZero() ? TradeMateStyle.Long : TradeMateStyle.Short);
        });

        bot.text("Active Bot").observe().take(1).to(v -> {
            view.market.register(new LazyBear());
        });

        table.mode(SelectionMode.MULTIPLE).render(table -> new CatalogRow()).context($ -> {
            Calculated<Boolean> ordersArePassive = Viewtify.calculate(table.selectedItems())
                    .flatVariable(o -> o.observeStateNow().to())
                    .isNot(ACTIVE);

            $.menu().text(Cancel).disableWhen(ordersArePassive).when(User.Action, e -> act(this::cancel));
        });

        side.text(SiDe)
                .model(Order.class, Order::direction)
                .render((label, side) -> label.text(side).styleOnly(TradeMateStyle.Side.of(side)));
        amount.text(Amount).modelByVar(Order.class, o -> o.observeRemainingSizeNow().to());
        price.text(Price).model(Order.class, o -> o.price);

        // initialize orders on server
        I.signal(view.market.orders.items).take(Order::isBuy).sort(Comparator.reverseOrder()).to(this::createOrderItem);
        I.signal(view.market.orders.items).take(Order::isSell).sort(Comparator.naturalOrder()).to(this::createOrderItem);

        // observe orders on clinet
        view.market.orders.add.to(this::createOrderItem);
    }

    /**
     * Support wheel change.
     * 
     * @param source
     * @param amount
     * @return
     */
    private WiseBiConsumer<ScrollEvent, UIText> changeBy(UISpinner<Num> spinner) {
        return (e, ui) -> {
            Num current = Num.of(ui.value());
            double deltaY = e.getDeltaY();

            if (deltaY > 0) {
                // increment
                ui.value(current.plus(spinner.value()).toString());
            } else if (deltaY < 0) {
                // decrement
                ui.value(Num.max(Num.ZERO, current.minus(spinner.value())).toString());
            }
            e.consume();
        };
    }

    /**
     * Request order by API.
     * 
     * @param side
     * @return
     */
    private void requestOrder(Direction side) {
        Viewtify.inWorker(() -> {
            // ========================================
            // Create Model
            // ========================================
            Num size = orderSize.valueOr(Num.ZERO);
            Num initSize = size;
            Num price = orderPrice.valueOr(Num.ZERO);
            int divideSize = orderDivideSize.value();
            int increaseInterval = orderDivideIntervalAmount.value();
            Num priceInterval = orderPriceInterval.valueOr(Num.ZERO).multiply(side.isBuy() ? -1 : 1);

            for (int i = 0; i < divideSize; i++) {
                Num optimizedSize = increaseInterval == 0 ? Num.ZERO
                        : Num.of(i).divide(increaseInterval).scale(0, RoundingMode.FLOOR).multiply(initSize.divide(2));
                Num optimizedPrice = view.market.orderBook.computeBestPrice(side, price, optimizeThreshold.value(), Num.of(2));

                Order order = Order.with.direction(side, size.plus(optimizedSize)).price(optimizedPrice).state(OrderState.REQUESTING);
                price = optimizedPrice.plus(priceInterval);

                view.market.request(order).to(I.NoOP);
            }
        });
    }

    /**
     * Create tree item for {@link OrderSet}.
     * 
     * @param set
     */
    private void createOrderItem(Order order) {
        if (order != null) {
            table.addItemAtLast(order);
            order.observeTerminating().on(Viewtify.UIThread).to(() -> table.removeItem(order));
        }
    }

    /**
     * Cancel {@link OrderSet} or {@link Order}.
     * 
     * @param order
     */
    private void act(Consumer<Order> forOrder) {
        for (Order order : table.selectedItems()) {
            forOrder.accept(order);
        }
    }

    /**
     * Cancel {@link Order}.
     * 
     * @param order
     */
    private void cancel(Order order) {
        Viewtify.inWorker(() -> {
            view.market.cancel(order).to(o -> {
            });
        });
    }

    /**
     * 
     */
    private class CatalogRow extends TableRow<Order> implements StyleHelper<CatalogRow, CatalogRow> {

        /**
         * 
         */
        private CatalogRow() {
            styleOnly(Viewtify.observing(itemProperty()).as(Order.class).switchMap(o -> o.observeStateNow()).map(S.State::of));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CatalogRow ui() {
            return this;
        }
    }

    /**
     * @version 2018/09/09 9:14:18
     */
    private interface S extends StyleDSL {

        Style Root = () -> {
            padding.left(5, px);
            display.width(250, px).height.fill();
            overflow.y.auto();
        };

        Style Row = () -> {
            padding.top(8, px);
            text.verticalAlign.middle();
        };

        Style Label = () -> {
            display.width(80, px);
            display.height(27, px);
            padding.top(5, px);
        };

        Style SFD = () -> {
            display.width(260, px);
            display.height(27, px);
            padding.top(5, px);
        };

        Style Form = () -> {
            display.maxWidth(80, px).height(27, px);
        };

        Style FormMin = () -> {
            display.maxWidth(72, px).height(27, px);
            margin.left(15, px);
        };

        Style FormButton = () -> {
            display.width(62, px).height(31, px);
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

        Style Catalog = () -> {
            display.height.fill();
        };

        Style Wide = () -> {
            display.width(120, px);
        };

        Style Narrow = () -> {
            display.width(65, px);
        };
    }
}
