/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.github.luben.zstd.ZstdOutputStream;
import com.jerolba.carpet.CarpetReader;
import com.jerolba.carpet.CarpetWriter;

import cointoss.market.binance.BinanceFuture;

public class AAA {

    public static void main(String[] args) throws IOException {

        try (OutputStream outputStream = new ZstdOutputStream(new FileOutputStream("my_file.parquet"), 5)) {
            try (CarpetWriter<MyRecord> writer = new CarpetWriter<>(outputStream, MyRecord.class)) {

                int[] count = {0};
                long start = System.currentTimeMillis();
                BinanceFuture.FUTURE_BTC_USDT.log.at(2024, 2, 10).to(e -> {
                    // System.out.println(count[0]++ + " " + e);
                    //
                    // try {
                    // writer.write(new MyRecord(e.id, e.price.floatValue(), e.size.floatValue(),
                    // e.isPositive(), e.mills));
                    // } catch (IOException e1) {
                    // throw I.quiet(e1);
                    // }
                });
                long end = System.currentTimeMillis();

                System.out.println(end - start);

                start = System.currentTimeMillis();
                new CarpetReader<>(new File("my_file.parquet"), MyRecord.class).forEach(e -> {

                });
                end = System.currentTimeMillis();
                System.out.println(end - start);
            }
        }
    }

    record MyRecord(long id, float price, float size, boolean buy, long mills) {
    }

    // public static void read(String[] args) throws IOException {
    // // メモリアロケータを作成
    // BufferAllocator allocator = new RootAllocator(Long.MAX_VALUE);
    //
    // // Parquetファイルのパス
    // File parquetFile = new File("user_arrow.parquet");
    //
    // // ArrowParquetReaderを使ってParquetファイルを読み込む
    // try (ArrowParquetReader<VectorSchemaRoot> reader = ArrowParquetReader.builder().build()) {
    //
    // // 読み込んだデータを保持するためのVectorSchemaRootを取得
    // VectorSchemaRoot root = reader.read();
    //
    // // スキーマを取得
    // Schema schema = root.getSchema();
    // System.out.println("Schema: " + schema);
    //
    // // データを処理
    // while (root.getRowCount() > 0) {
    // for (int i = 0; i < root.getRowCount(); i++) {
    // // 行ごとにデータを読み取る
    // String name = root.getVector("name").getObject(i).toString();
    // int age = Integer.parseInt(root.getVector("age").getObject(i).toString());
    //
    // // Optionalフィールドのemailをチェック
    // Object emailObj = root.getVector("email").getObject(i);
    // String email = (emailObj != null) ? emailObj.toString() : "(not provided)";
    //
    // System.out.println("Name: " + name);
    // System.out.println("Age: " + age);
    // System.out.println("Email: " + email);
    // System.out.println();
    // }
    //
    // // 次のブロックのデータを読み込む
    // root = reader.read();
    // }
    // }
    //
    // // メモリアロケータを閉じる
    // allocator.close();
    // }
}
