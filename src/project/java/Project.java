/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
public class Project extends bee.api.Project {

    String Log4j = "2.11.1";

    {
        product("cointoss", "cointoss", "1.0.1");

        require("com.github.teletha", "sinobu");
        require("com.github.teletha", "viewtify");
        require("com.github.teletha", "icymanipulator").atAnnotation();
        require("com.github.teletha", "antibug").atTest();
        require("org.apache.commons", "commons-lang3");
        require("org.apache.commons", "commons-math3");
        require("commons-net", "commons-net");
        require("org.apache.logging.log4j", "log4j-core", Log4j);
        require("org.apache.logging.log4j", "log4j-slf4j-impl", Log4j);
        // require("org.decimal4j", "decimal4j");
        require("com.google.guava", "guava");
        unrequire("com.google.code.findbugs", "jsr305");
        unrequire("com.google.errorprone", "error_prone_annotations");
        unrequire("com.google.guava", "listenablefuture");
        unrequire("com.google.j2objc", "j2objc-annotations");
        unrequire("org.checkerframework", "checker-qual");
        require("com.squareup.okhttp3", "okhttp", "3.14.1");
        require("com.univocity", "univocity-parsers");
        require("com.github.luben", "zstd-jni");

        versionControlSystem("https://github.com/teletha/cointoss");
    }
}