/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.order;

import static java.util.concurrent.TimeUnit.*;

import java.math.RoundingMode;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javafx.scene.control.Spinner;
import javafx.scene.input.ScrollEvent;

import cointoss.Side;
import cointoss.order.Order;
import cointoss.order.QuantityConditionsEnforcement;
import cointoss.order.OrderState;
import cointoss.util.Num;
import kiss.I;
import kiss.WiseBiConsumer;
import trademate.Notificator;
import trademate.TradingView;
import viewtify.UI;
import viewtify.User;
import viewtify.View;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UISpinner;
import viewtify.ui.UIText;

/**
 * @version 2018/02/07 17:11:55
 */
public class OrderBuilder extends View {

    private Predicate<UIText> positiveNumber = ui -> {
        try {
            return Num.of(ui.text()).isPositive();
        } catch (

        NumberFormatException e) {
            return false;
        }
    };

    private Predicate<UIText> negativeNumber = ui -> {
        try {
            return Num.of(ui.text()).isNegative();
        } catch (

        NumberFormatException e) {
            return false;
        }
    };

    /** UI */
    private @UI UIText orderSize;

    /** UI */
    private @UI UISpinner<Num> orderSizeAmount;

    /** UI */
    private @UI UIText orderPrice;

    /** UI */
    private @UI UISpinner<Num> orderPriceAmount;

    /** UI */
    private @UI UISpinner<Integer> orderDivideSize;

    /** UI */
    private @UI UISpinner<Integer> orderDivideIntervalAmount;

    /** UI */
    private @UI UISpinner<Num> optimizeThreshold;

    /** UI */
    private @UI UIText orderPriceInterval;

    /** UI */
    private @UI UISpinner<Num> orderPriceIntervalAmount;

    /** UI */
    private @UI UIButton orderLimitLong;

    /** UI */
    private @UI UIButton orderLimitShort;

    /** UI */
    private @UI UIButton orderRetreat;

    /** UI */
    private @UI UIButton orderReverse;

    /** UI */
    private @UI UIComboBox<QuantityConditionsEnforcement> orderQuantity;

    /** UI */
    private @UI TradingView view;

    /** UI */
    private @UI UICheckBox bot;

    private Notificator notificator = I.make(Notificator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        orderSize.initial("0").when(User.Scroll, changeBy(orderSizeAmount.ui)).require(positiveNumber);
        orderSizeAmount.values(3, Num.of("0.001"), Num.of("0.01"), Num.of("0.1"), Num.ONE);

        orderPrice.initial("0").when(User.Scroll, changeBy(orderPriceAmount.ui)).require(positiveNumber);
        orderPriceAmount.values(0, Num.ONE, Num.HUNDRED, Num.THOUSAND, Num.of(10000));

        orderDivideSize.values(0, IntStream.range(1, 31).boxed());
        orderDivideIntervalAmount.values(0, IntStream.range(0, 20).boxed()).disableWhen(orderDivideSize.ui.valueProperty().isEqualTo(1));
        optimizeThreshold.values(0, Num.range(0, 20));
        orderPriceInterval.initial("0")
                .when(User.Scroll, changeBy(orderPriceIntervalAmount.ui))
                .require(positiveNumber)
                .parent()
                .disableWhen(orderDivideSize.ui.valueProperty().isEqualTo(1));
        orderPriceIntervalAmount.values(0, Num.TEN, Num.HUNDRED, Num.THOUSAND);

        // validate order condition
        orderLimitLong.parent().disableWhen(orderSize.isInvalid().or(orderPrice.isInvalid()));

        orderLimitLong.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.BUY).to(this::requestOrder);
        orderLimitShort.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.SELL).to(this::requestOrder);
        orderQuantity.values(QuantityConditionsEnforcement.values()).initial(QuantityConditionsEnforcement.GoodTillCanceled);

        orderRetreat.when(User.Click).throttle(1000, MILLISECONDS).to(this::retreat);
        orderReverse.when(User.Click).throttle(1000, MILLISECONDS).to(this::reverse);
    }

    /**
     * Support wheel change.
     * 
     * @param source
     * @param amount
     * @return
     */
    private WiseBiConsumer<ScrollEvent, UIText> changeBy(Spinner<Num> spinner) {
        return (e, ui) -> {
            Num current = Num.of(ui.text());
            double deltaY = e.getDeltaY();

            if (deltaY > 0) {
                // increment
                ui.text(current.plus(spinner.getValue()));
            } else if (deltaY < 0) {
                // decrement
                ui.text(current.minus(spinner.getValue()));
            }
        };
    }

    /**
     * Request order by API.
     * 
     * @param side
     * @return
     */
    private void requestOrder(Side side) {
        OrderSet set = new OrderSet();

        // ========================================
        // Create Model
        // ========================================
        Num size = orderSize.valueOr(Num.ZERO);
        Num initSize = size;
        Num price = orderPrice.valueOr(Num.ZERO);
        int divideSize = orderDivideSize.value();
        int increaseInterval = orderDivideIntervalAmount.value();
        Num priceInterval = orderPriceInterval.valueOr(Num.ZERO).multiply(side.isBuy() ? -1 : 1);
        QuantityConditionsEnforcement quantity = orderQuantity.value();

        for (int i = 0; i < divideSize; i++) {
            Num optimizedSize = increaseInterval == 0 ? Num.ZERO
                    : Num.of(i).divide(increaseInterval).scale(0, RoundingMode.FLOOR).multiply(initSize.divide(2));
            Num optimizedPrice = view.market().orderBook.computeBestPrice(side, price, optimizeThreshold.value(), Num.of(2));

            Order order = Order.limit(side, size.plus(optimizedSize), optimizedPrice).type(quantity);
            order.state.set(OrderState.REQUESTING);
            order.isDisposed().to(() -> set.sub.remove(order));

            set.sub.add(order);

            price = optimizedPrice.plus(priceInterval);
        }
        view.order(set);
    }

    /**
     * Request retreat order.
     */
    private void retreat() {
    }

    /**
     * Request reverse order.
     */
    private void reverse() {
    }
}
