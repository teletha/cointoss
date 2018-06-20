/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import kiss.I;

/**
 * @version 2018/06/20 14:10:53
 */
public class AAA {

    public static void main(String[] args) {
        // return I.signal(dates)
        // .takeUntil(cacheLast::isBefore)
        // .flatMap(day -> new Cache(day).read())
        // .effect(e -> cacheId = e.id)
        // .take(e -> e.isAfter(start))
        // .concat(network().effect(this::cache));
        I.signal(10, v -> v + 3).takeUntil(v -> 30 < v).flatMap(v -> {
            return I.signal(v, v + 1, v + 2);
        }).effect(e -> {

        }).take(e -> e < 25).concat(I.signal(-10, -11, -12)).to(e -> {
            System.out.println(e);
        });
    }
}
