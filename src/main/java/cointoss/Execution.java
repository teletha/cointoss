/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.zip.CRC32;

import cointoss.order.Order;
import cointoss.util.Num;
import kiss.Decoder;
import kiss.Encoder;

/**
 * @version 2017/08/23 8:47:20
 */
public class Execution {

    public long id;

    /** Buyer id of this execution. */
    public String buy_child_order_acceptance_id = "";

    /** Seller id of this execution. */
    public String sell_child_order_acceptance_id = "";

    /** The side */
    public Side side;

    /** price */
    public Num price;

    /** size */
    public Num size;

    /** size */
    public Num cumulativeSize = Num.ZERO;

    /** date */
    public ZonedDateTime exec_date;

    /** INTERNAL USAGE */
    Order associated;

    /**
     * 
     */
    public Execution() {
    }

    /**
     * @param line
     */
    public Execution(String line) {
        this(line.split(" "));
    }

    /**
     * @param line
     */
    public Execution(String[] values) {
        id = Long.parseLong(values[0]);
        exec_date = LocalDateTime.parse(values[1]).atZone(cointoss.util.Chrono.UTC);
        side = Side.parse(values[2]);
        price = Num.of(values[3]);
        size = cumulativeSize = Num.of(values[4]);

        if (5 < values.length) {
            buy_child_order_acceptance_id = values[5];
            sell_child_order_acceptance_id = values[6];
        }
    }

    /**
     * @return
     */
    public final boolean isMine() {
        return associated != null;
    }

    /**
     * Helper method to compare date and time.
     * 
     * @param time
     * @return A result.
     */
    public final boolean isBefore(ZonedDateTime time) {
        return exec_date.isBefore(time);
    }

    /**
     * Helper method to compare date and time.
     * 
     * @param time
     * @return A result.
     */
    public final boolean isAfter(ZonedDateTime time) {
        return exec_date.isAfter(time);
    }

    /**
     * Compute buyer id.
     * 
     * @return
     */
    public final long buyer() {
        return id(buy_child_order_acceptance_id);
    }

    /**
     * Compute seller id.
     * 
     * @return
     */
    public final long seller() {
        return id(sell_child_order_acceptance_id);
    }

    private long id(String value) {
        if (value.startsWith("JRF20")) {
            return Long.parseLong(value.substring(11).replaceAll("-", ""));
        } else {
            CRC32 crc = new CRC32();
            crc.update(value.getBytes());
            return crc.getValue();
        }
    }

    /**
     * Calculate the after time.
     * 
     * @param seconds
     * @return
     */
    public ZonedDateTime after(long seconds) {
        return exec_date.plusSeconds(seconds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return id + " " + exec_date.toLocalDateTime() + " " + side
                .mark() + " " + price + " " + size + " " + buy_child_order_acceptance_id + " " + sell_child_order_acceptance_id;
    }

    /**
     * @version 2017/09/07 23:25:44
     */
    @SuppressWarnings("unused")
    private static class Codec implements Decoder<ZonedDateTime>, Encoder<ZonedDateTime> {

        private static final ZoneId zone = ZoneId.of("UTC");

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(ZonedDateTime value) {
            return value.toLocalDate().toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ZonedDateTime decode(String value) {
            return LocalDateTime.parse(value).atZone(zone);
        }
    }

}
