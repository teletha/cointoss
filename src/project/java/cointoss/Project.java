/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "cointoss", ref("version.txt"));

        require("com.github.teletha", "sinobu");
        require("com.github.teletha", "viewtify");
        require("com.github.teletha", "typewriter");
        require("com.github.teletha", "hypatia");
        require("com.github.teletha", "icymanipulator").atAnnotation();
        require("com.github.teletha", "antibug").atTest();
        require("com.pgs-soft", "HttpClientMock").atTest();
        require("org.decimal4j", "decimal4j").atTest();
        require("commons-codec", "commons-codec").atTest();
        require("org.apache.commons", "commons-lang3");
        require("commons-net", "commons-net");
        require("com.google.guava", "guava");
        // require("com.github.ben-manes.caffeine", "caffeine");
        unrequire("com.google.code.findbugs", "jsr305");
        unrequire("com.google.errorprone", "error_prone_annotations");
        unrequire("com.google.guava", "listenablefuture");
        unrequire("com.google.j2objc", "j2objc-annotations");
        unrequire("org.checkerframework", "checker-qual");
        require("com.univocity", "univocity-parsers");
        require("com.github.luben", "zstd-jni");

        require("org.xerial", "sqlite-jdbc");
        // require("com.h2database", "h2");
        require("org.duckdb", "duckdb_jdbc");
        require("org.apache.arrow", "arrow-vector");
        require("org.apache.arrow", "arrow-memory-netty");
        require("org.apache.parquet", "parquet-arrow");
        require("org.apache.parquet", "parquet-hadoop");
        require("org.apache.parquet", "parquet-common");
        require("org.slf4j", "slf4j-api", "2.1.0-alpha1");

        // require("io.fair-acc", "chartfx");
        // chartfx requires old jafafx, so override it
        // require("org.openjfx", "javafx-base");
        // require("org.openjfx", "javafx-controls");

        // require("org.nd4j", "nd4j-native-platform");
        // require("org.deeplearning4j", "rl4j-core");
    }
}