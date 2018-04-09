/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package influxdb;

import java.util.concurrent.TimeUnit;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;

import cointoss.Execution;
import filer.Filer;

/**
 * @version 2018/04/09 22:16:52
 */
public class User {

    public static void main(String[] args) {
        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
        String dbName = "bitflyer";
        influxDB.query(new Query("create database " + dbName, dbName));
        influxDB.setDatabase(dbName);

        influxDB.enableBatch(BatchOptions.DEFAULTS);

        Filer.read("F:\\Development\\CoinToss\\.log\\bitflyer\\FX_BTC_JPY\\execution20180404.log")
                .map(Execution::new)
                .buffer(1000)
                .to(exes -> {
                    BatchPoints batchPoints = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();

                    for (Execution exe : exes) {
                        String identity = String.valueOf(exe.id);
                        identity = identity.substring(identity.length() - 6);
                        long identicalDate = exe.exec_date.toInstant().toEpochMilli() * 100000 + Long.parseLong(identity);

                        Point build = Point.measurement("bitflyer-btcfx")
                                .time(identicalDate, TimeUnit.NANOSECONDS)
                                .addField("id", exe.id)
                                .addField("time", exe.exec_date.toInstant().toEpochMilli())
                                .addField("side", exe.side.mark())
                                .addField("price", exe.price.toDouble())
                                .addField("size", exe.size.toDouble())
                                .addField("buyID", exe.buy_child_order_acceptance_id)
                                .addField("sellID", exe.sell_child_order_acceptance_id)
                                .build();

                        batchPoints.point(build);
                    }
                    influxDB.write(batchPoints);
                });

        // influxDB.write(Point.measurement("cpu")
        // .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        // .addField("idle", 90L)
        // .addField("user", 9L)
        // .addField("system", 1L)
        // .build());
        //
        // influxDB.write(Point.measurement("disk")
        // .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        // .addField("used", 80L)
        // .addField("free", 1L)
        // .build());

        influxDB.close();
    }
}
