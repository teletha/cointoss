/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import kiss.I;

public class IcyManipulator {

    /**
     * Find field updater.
     * 
     * @param name A field name.
     * @return An updater.
     */
    public static MethodHandle updater(Class type, String name) {
        try {
            Field field = type.getField(name);
            field.setAccessible(true);

            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
