/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

public enum PriceRangedVolumeType {
    Total {
        /**
         * {@inheritDoc}
         */
        @Override
        public double max(double longVolumes, double shortVolumes) {
            return longVolumes + shortVolumes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double width(double longVolumes, double shortVolumes) {
            return longVolumes + shortVolumes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double scale() {
            return 0.75;
        }
    },

    Diff {
        /**
         * {@inheritDoc}
         */
        @Override
        public double max(double longVolumes, double shortVolumes) {
            return Math.max(longVolumes, shortVolumes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double width(double longVolumes, double shortVolumes) {
            return longVolumes - shortVolumes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double scale() {
            return 1;
        }
    },
    Both {
        /**
         * {@inheritDoc}
         */
        @Override
        public double max(double longVolumes, double shortVolumes) {
            return Math.max(longVolumes, shortVolumes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double width(double longVolumes, double shortVolumes) {
            return longVolumes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double scale() {
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
    public abstract double max(double longVolumes, double shortVolumes);

    /**
     * Compute the visual volume.
     * 
     * @param longVolumes
     * @param shortVolumes
     * @return
     */
    public abstract double width(double longVolumes, double shortVolumes);

    /**
     * Compute the base scale.
     */
    public abstract double scale();
}