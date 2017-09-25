/**
 * Copyright 2015-2017 Knowm Inc. (http://knowm.org) and contributors.
 * Copyright 2011-2015 Xeiam LLC (http://xeiam.com) and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cointoss.chart;

import java.awt.BorderLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.knowm.xchart.OHLCChart;
import org.knowm.xchart.OHLCChartBuilder;
import org.knowm.xchart.OHLCSeries;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.ChartTheme;

import cointoss.Execution;
import cointoss.market.bitflyer.BitFlyer;
import filer.Filer;

/**
 * Demonstrates the following:
 * <ul>
 * <li>HiLo render style
 * <li>LegendPosition.OutsideS
 * <li>Two YAxis Groups - both on left
 */
public class OHLCChart01 implements ExampleChart<OHLCChart> {

    public static void main(String[] args) {

        ExampleChart<OHLCChart> exampleChart = new OHLCChart01();
        OHLCChart chart = exampleChart.getChart();

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Create and set up the window.
            JFrame frame = new JFrame("Advanced Example");
            frame.setBounds(100, 100, 1000, chart.getHeight() + 200);
            frame.setLayout(new BorderLayout());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JScrollPane scrollPane = new JScrollPane();
            frame.add(scrollPane, BorderLayout.CENTER);

            // chart
            JPanel chartPanel = new XChartPanel<OHLCChart>(chart);
            scrollPane.setViewportView(chartPanel);

            // label
            JLabel label = new JLabel("Blah blah blah.", SwingConstants.CENTER);
            frame.add(label, BorderLayout.SOUTH);

            // Display the window.
            frame.setVisible(true);
        });
    }

    @Override
    public OHLCChart getChart() {
        Chart cc = chart(BitFlyer.FX_BTC_JPY, "2017-09-05T13:00:00", "2017-09-07T00:59:59", Duration.ofMinutes(1));

        // Create Chart
        OHLCChart chart = new OHLCChartBuilder().width(cc.ticks.size() * 8).height(500).title("Prices").theme(ChartTheme.Matlab).build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);

        List<Date> xData = new ArrayList<Date>();
        List<Double> openData = new ArrayList<Double>();
        List<Double> highData = new ArrayList<Double>();
        List<Double> lowData = new ArrayList<Double>();
        List<Double> closeData = new ArrayList<Double>();

        for (Tick tick : cc.ticks) {
            xData.add(Date.from(tick.start.toInstant()));
            openData.add(tick.openPrice.toDouble());
            highData.add(tick.maxPrice.toDouble());
            lowData.add(tick.minPrice.toDouble());
            closeData.add(tick.closePrice.toDouble());
        }

        xData = null;
        chart.addSeries("Series", xData, openData, highData, lowData, closeData).setRenderStyle(OHLCSeries.OHLCSeriesRenderStyle.Candle);
        chart.getStyler().setToolTipsEnabled(true);
        return chart;
    }

    private Chart chart(BitFlyer type, String start, String end, Duration duration) {
        // convert Asia/Tokyo to UTC
        ZonedDateTime startTime = LocalDateTime.parse(start).minusHours(9).atZone(Execution.UTC);
        ZonedDateTime endTime = LocalDateTime.parse(end).minusHours(9).atZone(Execution.UTC);

        // search tick log
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
        Path file = Filer.locate("src/test/resources/trend")
                .resolve(format.format(startTime) + "～" + format.format(endTime) + " " + duration + ".txt");

        Chart chart = new Chart(duration);

        if (Files.notExists(file)) {
            // crate new tick log from execution log
            type.log() //
                    .range(startTime, endTime)
                    .skipWhile(e -> e.isBefore(startTime))
                    .takeWhile(e -> e.isBefore(endTime))
                    .to(chart::tick);

            chart.writeTo(file);
        } else {
            chart.readFrom(file);
        }
        return chart;
    }

}
