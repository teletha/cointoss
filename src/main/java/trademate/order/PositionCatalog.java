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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;

import cointoss.Position;
import cointoss.Side;
import cointoss.order.Order;
import cointoss.order.OrderBookList;
import cointoss.util.Num;
import kiss.Signal;
import kiss.Variable;
import trademate.TradingView;
import viewtify.UI;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.bind.Calculation;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;

/**
 * @version 2017/12/20 14:37:27
 */
public class PositionCatalog extends View {

    /** The date formatter. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

    /** UI */
    private @UI UITableView<Position> positions;

    /** UI */
    private @UI UITableColumn<Position, ZonedDateTime> openPositionDate;

    /** UI */
    private @UI UITableColumn<Position, Side> openPositionSide;

    /** UI */
    private @UI UITableColumn<Position, Num> openPositionAmount;

    /** UI */
    private @UI UITableColumn<Position, Num> openPositionPrice;

    /** UI */
    private @UI UITableColumn<Position, Num> openPositionProfitAndLoss;

    /** Parent View */
    private @UI TradingView view;

    private PositionManager manager;

    private static <V> Signal<ObservableList<V>> signal(ObservableList<V> list) {
        return new Signal<>((observer, disposer) -> {
            InvalidationListener listener = e -> {
                observer.accept(list);
            };

            list.addListener(listener);

            return disposer.add(() -> {
                list.removeListener(listener);
            });
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        Calculation<Num> totalAmount = Viewtify.calculate(positions.values).map(p -> p.size).reduce(Num.ZERO, Num::plus);
        Calculation<Num> totalPrice = Viewtify.calculate(positions.values).reduce(Num.ZERO, (t, p) -> t.plus(p.price.multiply(p.size)));
        Calculation<Num> averagePrice = Viewtify.calculate(totalPrice, totalAmount, (total, amount) -> total.divide(amount).scale(0));
        Calculation<Num> totalProfit = Viewtify.calculate(positions.values).flatVariable(p -> {
            System.out.println(p.profit);
            return p.profit;
        }).reduce(Num.ZERO, Num::plus);

        manager = new PositionManager(positions.values);
        openPositionDate.model(o -> o.date).render((ui, item) -> ui.text(formatter.format(item)));
        openPositionSide.model(o -> o.side).render((ui, item) -> ui.text(item).styleOnly(item));
        openPositionAmount.modelByVar(o -> o.size).header(Viewtify.calculate("数量 ").concat(totalAmount).trim());
        openPositionPrice.model(o -> o.price).header(Viewtify.calculate("価格 ").concat(averagePrice).trim());
        openPositionProfitAndLoss.modelByVar(o -> o.profit).header(Viewtify.calculate("損益 ").concat(totalProfit).trim());
        positions.selectMultipleRows().context($ -> {
            $.menu("撤退").whenUserClick(() -> positions.selection().forEach(this::retreat));
        });

        Position pp = new Position();
        pp.side = Side.BUY;
        pp.date = ZonedDateTime.now();
        pp.price = Num.of(700000);
        pp.size = Variable.of(Num.TEN);

        Position minus = new Position();
        minus.side = Side.BUY;
        minus.date = ZonedDateTime.now();
        minus.price = Num.of(800000);
        minus.size = Variable.of(Num.ONE);

        Calculation<Num> a = Viewtify.calculate(pp.profit);
        Calculation<Num> b = Viewtify.calculate(minus.profit);

        view.market().yourExecution.startWith(pp, minus).on(Viewtify.UIThread).to(p -> {
            for (Position position : positions.values) {
                if (position.side == p.side) {
                    if (position.price.is(p.price)) {
                        position.size.set(p.size.v.plus(position.size));
                        return;
                    }
                } else {
                    Num diff = p.size.get().minus(position.size);

                    if (diff.isPositive()) {
                        p.size.set(diff);
                        position.size.set(Num.ZERO);
                    } else if (diff.isZero()) {
                        p.size.set(diff);
                        position.size.set(Num.ZERO);
                        break;
                    } else {
                        position.size.set(position.size.get().minus(p.size));
                        return;
                    }
                }
            }

            if (p.size.v.isPositive()) {
                Viewtify.inUI(() -> positions.values.add(p));
                p.size.observe().take(Num::isZero).on(Viewtify.UIThread).to(() -> positions.values.remove(p));
            }
        });

        view.market().latest.observe().on(Viewtify.UIThread).to(e -> {
            for (Position position : positions.values) {
                if (position.isBuy()) {
                    position.profit.set(e.price.minus(position.price).multiply(position.size).scale(0));
                } else {
                    position.profit.set(position.price.minus(e.price).multiply(position.size).scale(0));
                }
            }
        });
    }

    /**
     * Request exit order.
     * 
     * @param position
     */
    private void retreat(Position position) {
        OrderBookList book = view.market().orderBook.bookFor(position.inverse());
        Num price = book.computeBestPrice(Num.ZERO, Num.TWO);

        view.order(Order.limit(position.inverse(), position.size.v, price));
    }

    /**
     * @version 2018/02/15 16:16:52
     */
    private static class PositionManager {

        /** The manager. */
        private final ObservableList<Position> positions;

        /** The lock system. */
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        /**
         * @param positions
         */
        private PositionManager(ObservableList<Position> positions) {
            this.positions = positions;
        }

        /**
         * Add new position.
         * 
         * @param position
         */
        private void add(Position position) {
            lock.writeLock().lock();

            try {
                positions.add(position);
            } finally {
                lock.writeLock().unlock();
            }
        }

        /**
         * Add new position.
         * 
         * @param position
         */
        private void remove(Position position) {
            lock.writeLock().lock();

            try {
                positions.remove(position);
            } finally {
                lock.writeLock().unlock();
            }
        }

        /**
         * Add new position.
         * 
         * @param position
         */
        private void each(Consumer<Position> process) {
            lock.readLock().lock();

            try {
                positions.forEach(process);
            } finally {
                lock.readLock().unlock();
            }
        }
    }
}
