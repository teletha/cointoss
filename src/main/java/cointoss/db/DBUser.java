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

import java.util.ArrayList;
import java.util.List;

import cointoss.util.arithmetic.Num;
import io.questdb.griffin.SqlException;

public class DBUser {
    public static class Bean {
        public int id;

        public long time;

        public Num price;

        /**
         * @param id
         * @param value
         */
        public Bean(int id, long time) {
            this.id = id;
            this.time = time;
            this.price = Num.random(1, 10000);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Bean [id=" + id + ", time=" + time + ", price=" + price + "]";
        }
    }

    public static void main(String[] args) throws SqlException {
        List<Bean> beans = new ArrayList();
        for (int i = 11; i < 10000; i++) {
            Bean bean = new Bean(i, 1000000000L * i);
            beans.add(bean);
        }

        TimeseriseDatabase.clearTable("bean");

        TimeseriseDatabase<Bean> db = TimeseriseDatabase.create("bean", Bean.class, "time");
        db.insert(beans);
        db.select("where 9800 < id order by time desc limit 1").to(item -> System.out.println(item));
        System.out.println(db.max("price") + "   " + db.min("price") + "   " + db.avg("price") + "   " + db.sum("price"));
    }
}
