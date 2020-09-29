/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.volume;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cointoss.Directional;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.util.arithmetic.Num;

public class GlobalVolume {

    /** The volume manager. */
    private final ConcurrentHashMap<MarketService, double[]> services = new ConcurrentHashMap();

    /** The directional volume. */
    private double longs = 0;

    /** The directional volume. */
    private double shorts = 0;

    /**
     * Add volume.
     * 
     * @param service A market service of the additional volume.
     * @param e An {@link Execution} of the additional volume.
     */
    public final void add(MarketService service, Execution e) {
        add(service, e, e.size);
    }

    /**
     * Add volume.
     * 
     * @param service A market service of the additional volume.
     * @param side A side of the additional volume.
     * @param volume The volume to add.
     */
    public final void add(MarketService service, Directional side, Num volume) {
        add(service, side, volume.doubleValue());
    }

    /**
     * Add volume.
     * 
     * @param service A market service of the additional volume.
     * @param side A side of the additional volume.
     * @param volume The volume to add.
     */
    public final void add(MarketService service, Directional side, double volume) {
        double[] volumes = services.computeIfAbsent(service, key -> new double[2]);

        if (side.isBuy()) {
            volumes[0] += volume;
            longs += volume;
        } else {
            volumes[1] += volume;
            shorts += volume;
        }
    }

    /**
     * Retrieve the volume of the specified {@link MarketService}.
     * 
     * @param service A target service.
     * @return A total volume on long side.
     */
    public final double longVolumeAt(MarketService service) {
        double[] volumes = services.get(service);

        if (volumes == null) {
            return 0;
        } else {
            return volumes[0];
        }
    }

    /**
     * Retrieve the volume of the specified {@link MarketService}.
     * 
     * @param service A target service.
     * @return A total volume on short side.
     */
    public final double shortVolumeAt(MarketService service) {
        double[] volumes = services.get(service);

        if (volumes == null) {
            return 0;
        } else {
            return volumes[1];
        }
    }

    /**
     * Retrieve the total volume on all {@link MarketService}.
     * 
     * @return A total volume on long side.
     */
    public final double longVolume() {
        return longs;
    }

    /**
     * Retrieve the total volume on all {@link MarketService}.
     * 
     * @return A total volume on short side.
     */
    public final double shortVolume() {
        return shorts;
    }

    /**
     * Retrieve all volume data.
     * 
     * @return
     */
    public final Set<Entry<MarketService, double[]>> volumes() {
        return services.entrySet();
    }
}
