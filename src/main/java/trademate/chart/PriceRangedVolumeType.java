/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart;

public enum PriceRangedVolumeType {
    Total {
        /**
         * {@inheritDoc}
         */
        @Override
        double max(double longVolumes, double shortVolumes) {
            return longVolumes + shortVolumes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        double width(double longVolumes, double shortVolumes) {
            return longVolumes + shortVolumes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        double scale() {
            return 0.75;
        }
    },

    Diff {
        /**
         * {@inheritDoc}
         */
        @Override
        double max(double longVolumes, double shortVolumes) {
            return Math.max(longVolumes, shortVolumes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        double width(double longVolumes, double shortVolumes) {
            return longVolumes - shortVolumes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        double scale() {
            return 1;
        }
    },
    Separate {
        /**
         * {@inheritDoc}
         */
        @Override
        double max(double longVolumes, double shortVolumes) {
            return Math.max(longVolumes, shortVolumes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        double width(double longVolumes, double shortVolumes) {
            return longVolumes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        double scale() {
            return 0.4;
        }
    };

    /**
     * Compute the max volume.
     * 
     * @param longVolumes
     * @param shortVolumes
     * @return
     */
    abstract double max(double longVolumes, double shortVolumes);

    /**
     * Compute the visual volume.
     * 
     * @param longVolumes
     * @param shortVolumes
     * @return
     */
    abstract double width(double longVolumes, double shortVolumes);

    /**
     * Compute the base scale.
     */
    abstract double scale();
}
