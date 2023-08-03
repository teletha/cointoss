/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.volume;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cointoss.Directional;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.ticker.data.Liquidation;
import cointoss.util.arithmetic.Num;

public class GlobalVolume {

    /** The volume manager. */
    private final ConcurrentHashMap<MarketService, float[]> services = new ConcurrentHashMap();

    /** The directional volume. */
    private float longs = 0;

    /** The directional volume. */
    private float shorts = 0;

    /** The directional volume. */
    private float liquidatedLongs = 0;

    /** The directional volume. */
    private float liquidatedShorts = 0;

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
        add(service, side, volume.floatValue());
    }

    /**
     * Add volume.
     * 
     * @param service A market service of the additional volume.
     * @param side A side of the additional volume.
     * @param volume The volume to add.
     */
    public final void add(MarketService service, Directional side, float volume) {
        float[] volumes = services.computeIfAbsent(service, key -> new float[4]);

        if (side.isBuy()) {
            volumes[0] += volume;
            longs += volume;
        } else {
            volumes[1] += volume;
            shorts += volume;
        }
    }

    /**
     * Add volume.
     * 
     * @param service A market service of the additional volume.
     * @param e An {@link Execution} of the additional volume.
     */
    public final void add(MarketService service, Liquidation e) {
        float[] volumes = services.computeIfAbsent(service, key -> new float[4]);

        if (e.isBuy()) {
            volumes[2] += e.size;
            liquidatedLongs += e.size;
        } else {
            volumes[3] += e.size;
            liquidatedShorts += e.size;
        }
    }

    /**
     * Retrieve the volume of the specified {@link MarketService}.
     * 
     * @param service A target service.
     * @return A total volume on long side.
     */
    public final float longVolumeAt(MarketService service) {
        float[] volumes = services.get(service);

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
    public final float shortVolumeAt(MarketService service) {
        float[] volumes = services.get(service);

        if (volumes == null) {
            return 0;
        } else {
            return volumes[1];
        }
    }

    /**
     * Retrieve the volume of the specified {@link MarketService}.
     * 
     * @param service A target service.
     * @return A total volume on long side.
     */
    public final float liquidatedLongVolumeAt(MarketService service) {
        float[] volumes = services.get(service);

        if (volumes == null) {
            return 0;
        } else {
            return volumes[2];
        }
    }

    /**
     * Retrieve the volume of the specified {@link MarketService}.
     * 
     * @param service A target service.
     * @return A total volume on short side.
     */
    public final float liquidatedShortVolumeAt(MarketService service) {
        float[] volumes = services.get(service);

        if (volumes == null) {
            return 0;
        } else {
            return volumes[3];
        }
    }

    /**
     * Retrieve the total volume on all {@link MarketService}.
     * 
     * @return A total volume on long side.
     */
    public final float longVolume() {
        return longs;
    }

    /**
     * Retrieve the total volume on all {@link MarketService}.
     * 
     * @return A total volume on short side.
     */
    public final float shortVolume() {
        return shorts;
    }

    /**
     * Retrieve the total volume on all {@link MarketService}.
     * 
     * @return A total volume on long side.
     */
    public final float liquidatedLongVolume() {
        return liquidatedLongs;
    }

    /**
     * Retrieve the total volume on all {@link MarketService}.
     * 
     * @return A total volume on short side.
     */
    public final float liquidatedShortVolume() {
        return liquidatedShorts;
    }

    /**
     * Retrieve all volume data.
     * 
     * @return
     */
    public final Set<Entry<MarketService, float[]>> volumes() {
        return services.entrySet();
    }
}