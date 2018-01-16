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
import trademate.TradingView;
import viewtify.UI;
import viewtify.View;
import viewtify.ui.UITreeTableColumn;
import viewtify.ui.UITreeTableView;

/**
 * @version 2017/12/20 14:37:27
 */
public class PositionCatalog extends View {

    /** The date formatter. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

    /** UI */
    private @UI UITreeTableView<Position> openPositionCatalog;

    /** UI */
    private @UI UITreeTableColumn<Position, ZonedDateTime> openPositionDate;

    /** UI */
    private @UI UITreeTableColumn<Position, Side> openPositionSide;

    /** UI */
    private @UI UITreeTableColumn<Position, Num> openPositionAmount;

    /** UI */
    private @UI UITreeTableColumn<Position, Num> openPositionPrice;

    /** Parent View */
    private @UI TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        openPositionDate.model(o -> o.open_date).render((ui, item) -> ui.text(formatter.format(item)));
        openPositionSide.model(o -> o.side).render((ui, item) -> ui.text(item).styleOnly(item));
        openPositionAmount.modelByVar(o -> o.size);
        openPositionPrice.model(o -> o.price);

        view.market().yourExecution.to(p -> {
            for (Position position : openPositionCatalog.root.values()) {
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
                openPositionCatalog.root.createItem(p).removeWhen(p.size.observe().take(Num::isZero));
            }
        });
    }
}
