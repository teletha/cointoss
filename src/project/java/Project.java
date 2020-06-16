/*
 * Copyright (C) 2019 CoinToss Development Team
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
        product("cointoss", "cointoss", "1.0");

        require("com.github.teletha", "sinobu");
        require("com.github.teletha", "viewtify");
        require("com.github.teletha", "necromancy");
        require("com.github.teletha", "icymanipulator").atAnnotation();
        require("com.github.teletha", "antibug").atTest();
        require("org.apache.commons", "commons-lang3");
        require("org.apache.commons", "commons-math3");
        require("commons-codec", "commons-codec");
        require("org.apache.logging.log4j", "log4j-core", Log4j);
        require("org.apache.logging.log4j", "log4j-slf4j-impl", Log4j);
        require("org.magicwerk", "brownies-collections");
        require("com.univocity", "univocity-parsers");
        require("com.github.luben", "zstd-jni");
        // require("org.decimal4j", "decimal4j");
        require("com.google.code.gson", "gson");
        require("com.squareup.okhttp3", "okhttp", "3.14.1");
        require("org.eclipse.collections", "eclipse-collections");
        require("org.ta4j", "ta4j-core");

        versionControlSystem("https://github.com/teletha/cointoss");
    }
}
