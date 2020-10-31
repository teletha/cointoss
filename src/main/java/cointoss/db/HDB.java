/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class HDB {

    public static void main(String[] args) throws Exception {
        // delete the database named 'test' in the user home directory
        // DeleteDbFiles.execute(".", "db", true);

        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:file:./db");
        Statement stat = conn.createStatement();

        // this line would initialize the database
        // from the SQL script file 'init.sql'
        // stat.execute("runscript from 'init.sql'");

        // stat.execute("create table test(id int primary key, time long)");
        //
        // PreparedStatement ps = conn.prepareStatement("insert into test values(?, ?)");
        // for (int i = 0; i < 100000000; i++) {
        // ps.setInt(1, i);
        // ps.setLong(2, i);
        // ps.executeUpdate();
        //
        // if (i % 10000 == 0) {
        // conn.commit();
        // }
        // }
        ResultSet rs;
        rs = stat.executeQuery("select sum(time) from test");
        System.out.println(rs);
        while (rs.next()) {
            System.out.println(rs.getLong(1));
        }
        stat.close();
        conn.close();
    }
}
