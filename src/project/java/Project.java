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
        product("cointoss", "CoinToss", "1.0");

        require("com.github.teletha", "sinobu", "1.0");
        require("com.github.teletha", "viewtify", "1.0");
        require("com.github.teletha", "marionette", "0.2");
        require("com.github.teletha", "antibug", "0.6").atTest();
        require("org.apache.commons", "commons-lang3", "3.7");
        require("commons-codec", "commons-codec", "1.11");
        require("org.apache.logging.log4j", "log4j-core", Log4j);
        require("org.apache.logging.log4j", "log4j-slf4j-impl", Log4j);
        require("org.magicwerk", "brownies-collections", "0.9.13");
        require("com.univocity", "univocity-parsers", "2.7.6");
        require("com.github.luben", "zstd-jni", "1.3.7-2");
        require("org.decimal4j", "decimal4j", "1.0.3");
        require("com.squareup.okhttp3", "okhttp", "3.12.0");
        require("com.github.signalr4j", "signalr4j", "2.0.3");
        require("org.immutables", "value", "2.6.3").atProvided();
    }
}
