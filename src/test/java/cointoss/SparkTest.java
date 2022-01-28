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

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import cointoss.execution.ExecutionLog;
import cointoss.market.bitflyer.BitFlyer;
import scala.Tuple2;

public class SparkTest {

    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("NetworkWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(10));

        JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);
        // Split each line into words
        JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(x.split(" ")).iterator());

        JavaPairDStream<String, Integer> pairs = words.mapToPair(s -> new Tuple2<>(s, 1));
        JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey((i1, i2) -> i1 + i2);

        // Print the first ten elements of each RDD generated in this DStream to the console
        wordCounts.print();

        jssc.start(); // Start the computation
        jssc.awaitTermination(); // Wait for the computation to terminate
    }

    public static void main2(String[] args) {
        ExecutionLog log = new ExecutionLog(BitFlyer.FX_BTC_JPY);

        SparkSession spark = SparkSession.builder().master("local").appName("Test").getOrCreate();
        Dataset<Row> data = spark.read().option("delimiter", " ").csv(".log/BitFlyer/FX_BTC_JPY/execution20220128.log").cache();

        long buy = data.filter((FilterFunction<Row>) s -> s.getString(2).equals("B")).count();
        long sell = data.filter((FilterFunction<Row>) s -> s.getString(2).equals("S")).count();
        System.out.println(buy + "  " + sell);

        spark.stop();
    }
}
