/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.magicwerk.brownies.collections.GapList;

import cointoss.Execution;
import cointoss.util.Num;
import kiss.Variable;

/**
 * @version 2018/07/05 10:37:46
 */
public class RealtimeTicker {

    /** The minimum interval. */
    private static final Duration interval = Duration.ofMillis(1000);

    /** The volume. */
    public Num volume = Num.ZERO;

    /** Volume of the period */
    public Num longVolume = Num.ZERO;

    /** Volume of the period */
    public Num longPriceIncrese = Num.ZERO;

    /** Volume of the period */
    public Num shortVolume = Num.ZERO;

    /** Volume of the period */
    public Num shortPriceDecrease = Num.ZERO;

    /** The recorder. */
    private final GapList<Execution> buffer = GapList.create();

    /** The tick span. */
    private final TickSpan span;

    private ZonedDateTime checkTime;

    private final Variable<Execution> latest;

    /**
     * 
     */
    public RealtimeTicker(TickSpan span, Variable<Execution> latest) {
        this.span = span;
        this.latest = latest;
        buffer.addLast(Execution.BASE);
    }

    void update(Execution incoming) {
        buffer.addLast(incoming);

        // incoming
        volume = volume.plus(incoming.size);

        if (incoming.side.isBuy()) {
            longVolume = longVolume.plus(incoming.size);
            longPriceIncrese = longPriceIncrese.plus(incoming.price.minus(latest.v.price));
        } else {
            shortVolume = shortVolume.plus(incoming.size);
            shortPriceDecrease = shortPriceDecrease.plus(latest.v.price.minus(incoming.price));
        }

        // outgoing
        ZonedDateTime threshold = incoming.date.minus(span.duration);
        Execution first = buffer.peek();

        while (first.date.isBefore(threshold)) {
            Execution outgoing = buffer.remove();
            Execution second = buffer.peek();

            volume = volume.minus(outgoing.size);

            if (outgoing.side.isBuy()) {
                longVolume = longVolume.minus(outgoing.size);
                longPriceIncrese = longPriceIncrese.minus(second.price.minus(outgoing.price));
            } else {
                shortVolume = shortVolume.minus(outgoing.size);
                shortPriceDecrease = shortPriceDecrease.minus(outgoing.price.minus(second.price));
            }

            // check next
            first = second;
        }
    }

    /**
     * Compute volume diff.
     * 
     * @return
     */
    public Num volume() {
        return longVolume.minus(shortVolume);
    }

    /**
     * Compute indicator.
     * 
     * @return
     */
    public Num estimateUpPotential() {
        return longVolume.isZero() ? Num.ZERO : longPriceIncrese.divide(longVolume).scale(3);
    }

    /**
     * Compute indicator.
     * 
     * @return
     */
    public Num estimateDownPotential() {
        return shortVolume.isZero() ? Num.ZERO : shortPriceDecrease.divide(shortVolume).scale(3);
    }
}
