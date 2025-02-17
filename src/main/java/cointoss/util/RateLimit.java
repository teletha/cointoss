/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import static java.util.concurrent.TimeUnit.*;

import java.util.HashMap;
import java.util.Map;

import kiss.I;
import kiss.Managed;
import kiss.Signaling;
import kiss.Singleton;
import kiss.Storable;

@Managed(Singleton.class)
class RateLimit implements Storable<RateLimit> {

    /** The saving request. */
    static final Signaling SAVE = new Signaling();

    static {
        SAVE.expose.throttle(15, SECONDS).to(() -> {
            I.make(RateLimit.class).store();
        });
    }

    public Map<String, Rate> rate = new HashMap();

    /**
     * Hide constructor.
     */
    private RateLimit() {
        restore();
    }

    /**
     * The limit rate info.
     */
    static class Rate {

        public long usingPermits;

        public long lastAccessedTime;
    }
}
