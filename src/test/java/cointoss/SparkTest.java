/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;

public class SparkTest {

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder().appName("Test").getOrCreate();
        Dataset<String> log = spark.read().textFile(".log/BitFlyer/BTC_JPY/execution20220127.log").cache();

        long buy = log.filter((FilterFunction<String>) s -> s.contains("B")).count();
        long sell = log.filter((FilterFunction<String>) s -> s.contains("S")).count();
        System.out.println(buy + "  " + sell);

        spark.stop();
    }
}
