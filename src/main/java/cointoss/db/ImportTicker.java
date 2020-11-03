/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.db;

import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog.LogType;
import cointoss.market.binance.Binance;

public class ImportTicker {

    public static void main(String[] args) {
        // Binance.BTC_USDT.log.rangeAll().to(e -> {
        // System.out.println(e);
        // });
        hour(Binance.BTC_USDT);
    }

    private static void importTicker(MarketService service) {
        String name = service.exchange + service.marketName.replace("/", "");

        TimeseriseDatabase.clearTable(name);

        TimeseriseDatabase<Execution> db = TimeseriseDatabase.create(name, Execution.class);

        service.log.rangeAll(LogType.Normal).to(e -> db.insert(e));
    }

    private static void hour(MarketService service) {
        TimeseriseDatabase<Execution> db = TimeseriseDatabase
                .create(service.exchange + service.marketName.replace("/", ""), Execution.class);
        System.out.println(db.count() + "   AvgPrice:" + db.avg("price") + " MinPrice:" + db.min("price") + "  MaxPrice:" + db
                .max("price") + "  TotalSize:" + db.sum("size") + "  MaxSize:" + db.max("size") + " MinSize:" + db.min("size"));
    }
}
