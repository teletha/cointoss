/*
 * Copyright (C) 2017 CoinToss Development Team
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

        repository("https://oss.sonatype.org/content/repositories/snapshots");
        require("com.github.teletha", "sinobu", "1.0");
        require("com.github.teletha", "filer", "0.5");
        require("com.github.teletha", "antibug", "0.3").atTest();
        require("org.apache.commons", "commons-lang3", "3.5");
        require("commons-codec", "commons-codec", "1.10");
        require("org.apache.httpcomponents", "httpclient", "4.5.3");
        // require("org.apache.logging.log4j", "log4j-core", "2.8.2");
        // require("org.apache.logging.log4j", "log4j-jul", "2.8.2");
        require("org.eclipse.collections", "eclipse-collections", "8.2.0");
        require("com.pubnub", "pubnub", "4.6.5");
        require("org.knowm.xchart", "xchart", "3.5.1-SNAPSHOT");
        require("org.magicwerk", "brownies-collections", "0.9.13");
        require("org.controlsfx", "controlsfx", "8.40.14");
        require("org.fxmisc.easybind", "easybind", "1.0.3");
    }
}
