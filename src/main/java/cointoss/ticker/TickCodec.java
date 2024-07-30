/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.nio.ByteBuffer;

import cointoss.util.feather.DataCodec;

class TickCodec extends DataCodec<Tick> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return 8 * 9;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tick read(long time, ByteBuffer reader) {
        Tick tick = new Tick(time, reader.getDouble());
        tick.closePrice = reader.getDouble();
        tick.highPrice = reader.getDouble();
        tick.lowPrice = reader.getDouble();
        tick.longVolume = reader.getDouble();
        tick.shortVolume = reader.getDouble();
        tick.longLosscutVolume = reader.getDouble();
        tick.shortLosscutVolume = reader.getDouble();
        return tick;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(Tick item, ByteBuffer writer) {
        writer.putLong(item.openTime);
        writer.putDouble(item.openPrice);
        writer.putDouble(item.closePrice);
        writer.putDouble(item.highPrice);
        writer.putDouble(item.lowPrice);
        writer.putDouble(item.longVolume);
        writer.putDouble(item.shortVolume);
        writer.putDouble(item.longLosscutVolume);
        writer.putDouble(item.shortLosscutVolume);
    }
}
