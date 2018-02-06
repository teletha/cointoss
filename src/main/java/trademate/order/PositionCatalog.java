/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.order;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import cointoss.Position;
import cointoss.Side;
import cointoss.util.Num;
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        Calculation<Num> totalAmount = Viewtify.calculate(positions.values).map(p -> p.size).reduce(Num.ZERO, Num::plus);
        Calculation<Num> totalPrice = Viewtify.calculate(positions.values).reduce(Num.ZERO, (t, p) -> t.plus(p.price.multiply(p.size)));
        Calculation<Num> averagePrice = Viewtify.calculate(totalPrice, totalAmount, (total, amount) -> total.divide(amount).scale(0));
        Calculation<Num> totalPnL = Viewtify.calculate(positions.values)
                .flatVariable(p -> p.profit)
                .reduce(Num.ZERO, Num::plus)
                .map(v -> v.scale(0));

        openPositionDate.model(o -> o.date).render((ui, item) -> ui.text(formatter.format(item)));
        openPositionSide.model(o -> o.side).render((ui, item) -> ui.text(item).styleOnly(item));
        openPositionAmount.modelByVar(o -> o.size).header(Viewtify.calculate("数量 ").concat(totalAmount).trim());
        openPositionPrice.model(o -> o.price).header(Viewtify.calculate("価格 ").concat(averagePrice).trim());
        openPositionProfitAndLoss.modelByVar(o -> o.profit).header(Viewtify.calculate("損益 ").concat(totalPnL).trim());
        positions.context($ -> {
            $.menu("撤退").whenUserClick(e -> {

            });
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

        view.market().yourExecution.startWith(pp, minus).to(p -> {
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
                positions.values.add(p);
                p.size.observe().take(Num::isZero).to(() -> positions.values.remove(p));
            }
        });

        view.market().latestPrice.observe().to(e -> {
            for (Position position : positions.values) {
                if (position.isBuy()) {
                    position.profit.set(e.minus(position.price).multiply(position.size));
                } else {
                    position.profit.set(position.price.minus(e).multiply(position.size));
                }
            }
        });
    }
}
