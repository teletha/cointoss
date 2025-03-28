/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.zip.GZIPInputStream;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import cointoss.MarketService;
import kiss.I;
import kiss.Signal;

public abstract class HttpLogHouse extends LogHouse {

    protected HttpLogHouse(MarketService service) {
        super(service);
    }

    /**
     * Locate URL by the specified date.
     * 
     * @param date
     * @return
     */
    public abstract String locate(ZonedDateTime date);

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean has(ZonedDateTime date) {
        Builder request = HttpRequest.newBuilder(URI.create(locate(date))).HEAD();

        return I.http(request, HttpResponse.class).waitForTerminate().map(HttpResponse::statusCode).recover(404).to().is(200);
    }

    /**
     * Helper method to parse the CSV source.
     * 
     * @param date
     * @return
     */
    public final Signal<String[]> downloadCSV(ZonedDateTime date) {
        CsvParserSettings setting = new CsvParserSettings();
        setting.getFormat().setDelimiter(',');
        setting.getFormat().setLineSeparator("\n");
        setting.setHeaderExtractionEnabled(true);
        CsvParser parser = new CsvParser(setting);

        return I.http(locate(date), InputStream.class)
                .flatIterable(in -> parser.iterate(new GZIPInputStream(in), StandardCharsets.ISO_8859_1))
                .effectOnComplete(parser::stopParsing);
    }
}