/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
public class Project extends bee.api.Project {

    {
        product("cointoss", "CoinToss", "1.0");

        require("com.github.teletha", "sinobu", "1.0");
        require("com.github.teletha", "viewtify", "1.0");
        require("com.github.teletha", "filer", "0.5");
        require("com.github.teletha", "marionette", "0.1");
        require("com.github.teletha", "antibug", "0.6").atTest();
        require("org.apache.commons", "commons-lang3", "3.7");
        require("commons-codec", "commons-codec", "1.11");
        require("org.apache.logging.log4j", "log4j-core", "2.10.0");
        require("org.apache.logging.log4j", "log4j-jul", "2.10.0");
        require("org.slf4j", "slf4j-simple", "1.7.22");
        require("com.pubnub", "pubnub-gson", "4.19.0");
        require("org.magicwerk", "brownies-collections", "0.9.13");
        require("org.elasticsearch.client", "elasticsearch-rest-high-level-client", "6.2.3");
    }
}
