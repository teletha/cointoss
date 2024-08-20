/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.List;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;

import kiss.I;

public interface NestableExtension extends Extension, BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

    /**
     * Collect internal extensions.
     * 
     * @return
     */
    default List<Extension> collectExtensions() {
        return I.signal(AnnotationUtils
                .findAnnotatedFields(getClass(), RegisterExtension.class, I::accept, HierarchyTraversalMode.BOTTOM_UP)).map(x -> {
                    x.setAccessible(true);
                    return (Extension) x.get(this);
                }).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void afterEach(ExtensionContext context) throws Exception {
        collectExtensions().forEach(x -> {
            if (x instanceof BeforeEachCallback each) {
                try {
                    each.beforeEach(context);
                } catch (Exception e) {
                    throw I.quiet(e);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void beforeEach(ExtensionContext context) throws Exception {
        collectExtensions().forEach(x -> {
            if (x instanceof AfterEachCallback each) {
                try {
                    each.afterEach(context);
                } catch (Exception e) {
                    throw I.quiet(e);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void beforeAll(ExtensionContext context) throws Exception {
        collectExtensions().forEach(x -> {
            if (x instanceof BeforeAllCallback each) {
                try {
                    each.beforeAll(context);
                } catch (Exception e) {
                    throw I.quiet(e);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void afterAll(ExtensionContext context) throws Exception {
        collectExtensions().forEach(x -> {
            if (x instanceof AfterAllCallback each) {
                try {
                    each.afterAll(context);
                } catch (Exception e) {
                    throw I.quiet(e);
                }
            }
        });
    }
}
