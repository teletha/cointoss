/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import cointoss.util.arithmetic.Num;

public abstract class NumIndicator extends AbstractNumberIndicator<Num, NumIndicator> {

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected NumIndicator(Ticker ticker) {
        this(ticker, tick -> {
            Tick rounded = ticker.ticks.at(tick.openTime);
            return rounded == null ? ticker.ticks.first() : rounded;
        });
    }

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected NumIndicator(Ticker ticker, Function<Tick, Tick> normalizer) {
        super(ticker, normalizer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num valueAt(Tick timestamp) {
        return valueAtRounded(normalizer.apply(timestamp));
    }

    /**
     * Return the value of this {@link NumIndicator}. It is ensure that the {@link Tick} parameter
     * is rounded for {@link Ticker}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    protected abstract Num valueAtRounded(Tick tick);

    /**
     * {@inheritDoc}
     */
    @Override
    protected NumIndicator build(BiFunction<Tick, NumIndicator, Num> delegator) {
        return new NumIndicator(ticker, normalizer) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                return delegator.apply(tick, this);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator scale(int size) {
        return new NumIndicator(ticker, normalizer) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                return NumIndicator.this.valueAt(tick).scale(size);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator ema(int size) {
        double multiplier = 2.0 / (size + 1);

        return memoize((size + 1) * 4, (tick, self) -> {
            Tick before = ticker.ticks.before(tick);
            if (before == null) {
                return valueAt(tick);
            }

            double prev = self.apply(before).doubleValue();
            double now = valueAt(tick).doubleValue();

            return Num.of(((now - prev) * multiplier) + prev);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator mma(int size) {
        double multiplier = 1.0 / size;

        return memoize((size + 1) * 4, (tick, self) -> {
            Tick before = ticker.ticks.before(tick);
            if (before == null) {
                return valueAt(tick);
            }

            double prev = self.apply(before).doubleValue();
            double now = valueAt(tick).doubleValue();

            return Num.of(((now - prev) * multiplier) + prev);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator sma(int size) {
        return new NumIndicator(ticker, normalizer) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                double value = 0;
                List<Tick> before = ticker.ticks.query(tick, o -> o.reverse().max(size)).toList();
                int actualSize = before.size();
                for (int i = 0; i < actualSize; i++) {
                    value += NumIndicator.this.valueAt(before.get(i)).doubleValue();
                }
                return Num.of(value / actualSize);
            }
        }.memoize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator wma(int size) {
        return new NumIndicator(ticker, normalizer) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                double value = 0;
                List<Tick> previous = ticker.ticks.query(tick, o -> o.reverse().max(size)).toList();
                int actualSize = previous.size();

                for (int i = 0; i < actualSize; i++) {
                    value += NumIndicator.this.valueAt(previous.get(i)).doubleValue() * (actualSize - i);
                }

                return Num.of(value / (actualSize * (actualSize + 1) / 2));
            }
        }.memoize();
    }

    /**
     * <p>
     * The average true range (ATR) is a technical analysis indicator that measures market
     * volatility by decomposing the entire range of an asset price for that period. Specifically,
     * ATR is a measure of volatility introduced by market technician J. Welles Wilder Jr. in his
     * book, "New Concepts in Technical Trading Systems."
     * </p>
     * <p>
     * The true range indicator is taken as the greatest of the following: current high less the
     * current low; the absolute value of the current high less the previous close; and the absolute
     * value of the current low less the previous close. The average true range is then a moving
     * average, generally using 14 days, of the true ranges.
     * </p>
     * 
     * @param ticker
     * @param size
     * @return
     */
    public static NumIndicator averageTrueRange(Ticker ticker, int size) {
        return trueRange(ticker).mma(size);
    }

    /**
     * <p>
     * Welles Wilder described these calculations to determine the trading range for a stock or
     * commodity. True Range is defined as the largest of the following:
     * </p>
     * <ul>
     * <li>The distance from today's high to today's low.</li>
     * <li>The distance from yesterday's close to today's high.</li>
     * <li>The distance from yesterday's close to today's low.</li>
     * </ul>
     * <p>
     * Wilder included price comparisons among subsequent bars in order to account for gaps in his
     * range calculation.
     * <p>
     * <p>
     * The raw True Range is then smoothed (a 14-period smoothing is common) to give an Average True
     * Range (ATR). The True Range can be smoothed using a variety of moving average types,
     * including Simple, Exponential, Welles Wilder, etc.
     * </p>
     * <p>
     * ATR measures a security's volatility. It does not indicate price direction or duration,
     * rather the degree of price movement. Average True Range can be interpreted using the same
     * techniques that are used with the other volatility indicators.
     * </p>
     * <p>
     * Wilder states that high values of ATR often occur at market bottoms following a sell-off. Low
     * ATR values are often found during extended sideways or consolidation periods.
     * </p>
     * <p>
     * Several other indicators are built off True Range, including DI+/DI-, ADX, and ADXR. RTL
     * Tokens . . . TR ( more )
     * </p>
     * 
     * @param ticker
     * @return
     */
    public static NumIndicator trueRange(Ticker ticker) {
        return build(ticker, tick -> {
            Num highLow = Num.of(tick.highPrice() - tick.lowPrice());
            Tick previous = ticker.ticks.before(tick);

            if (previous == null) {
                return highLow;
            }

            Num highClose = Num.of(tick.highPrice() - previous.lowPrice()).abs();
            Num closeLow = Num.of(previous.closePrice() - tick.lowPrice()).abs();

            return Num.max(highLow, highClose, closeLow);
        });
    }

    /**
     * Build your original {@link NumIndicator}.
     * 
     * @param ticker
     * @param calculator
     * @return
     */
    public static NumIndicator build(Ticker ticker, Function<Tick, Num> calculator) {
        Objects.requireNonNull(ticker);
        Objects.requireNonNull(calculator);

        return new NumIndicator(ticker) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                return calculator.apply(tick);
            }
        };
    }
}