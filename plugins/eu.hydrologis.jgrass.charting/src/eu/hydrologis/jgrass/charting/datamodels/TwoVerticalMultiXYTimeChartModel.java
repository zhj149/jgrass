/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
  package eu.hydrologis.jgrass.charting.datamodels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */

public class TwoVerticalMultiXYTimeChartModel implements Serializable {
    private final List<TwoVerticalMultiXYTimeChartItem> chartsData = new ArrayList<TwoVerticalMultiXYTimeChartItem>();

    /*
     * this object can chart the data
     */
    public ChartCreator creator = null;

    /**
     * @deprecated no more used, kept for ideas. <b>use {@link NumericChartData} instead</b>
     * @param chartNum this number will define how many different charts will be created inside this
     *        chartdata object
     */
    public TwoVerticalMultiXYTimeChartModel( int chartNum, ChartCreator creator ) {
        this.creator = creator;
        for( int i = 0; i < chartNum; i++ ) {
            chartsData.add(new TwoVerticalMultiXYTimeChartItem());
        }
    }

    /**
     * @param chartNum the chart item number to which add the data. Keeping the number constant adds
     *        the data to the same chart container. Changing the number triggers the creation of
     *        tabbs with different charts
     * @param lowerChartSerieTitle
     * @param lowerChartSerieXTitle
     * @param lowerChartSerieYTitle
     * @param lowerChartSerieData
     */
    public void addLowerChartSeriesData( int chartNum, String lowerChartSerieTitle,
            String lowerChartSerieXTitle, String lowerChartSerieYTitle,
            double[][] lowerChartSerieData ) {
        chartsData.get(chartNum).lowerChartSeriesData.add(lowerChartSerieData);
        chartsData.get(chartNum).lowerChartSeriesTitles.add(lowerChartSerieTitle);
        chartsData.get(chartNum).lowerChartSeriesXTitles.add(lowerChartSerieXTitle);
        chartsData.get(chartNum).lowerChartSeriesYTitles.add(lowerChartSerieYTitle);

    }

    /**
     * @param chartNum the chart item number to which add the data. Keeping the number constant adds
     *        the data to the same chart panel. Changing the number triggers the creation of tabbs
     *        with different charts
     * @param upperChartSerieTitle
     * @param upperChartSerieXTitle
     * @param upperChartSerieYTitle
     * @param upperChartSerieData
     */
    public void addUpperChartSeriesData( int chartNum, String upperChartSerieTitle,
            String upperChartSerieXTitle, String upperChartSerieYTitle,
            double[][] upperChartSerieData ) {
        chartsData.get(chartNum).upperChartSeriesData.add(upperChartSerieData);
        chartsData.get(chartNum).upperChartSeriesTitles.add(upperChartSerieTitle);
        chartsData.get(chartNum).upperChartSeriesXTitles.add(upperChartSerieXTitle);
        chartsData.get(chartNum).upperChartSeriesYTitles.add(upperChartSerieYTitle);
    }

    /**
     * empty the chartdata
     */
    public void resetData() {
        for( TwoVerticalMultiXYTimeChartItem cData : chartsData ) {
            cData.lowerChartSeriesData.clear();
            cData.lowerChartSeriesTitles.clear();
            cData.lowerChartSeriesXTitles.clear();
            cData.lowerChartSeriesYTitles.clear();
            cData.upperChartSeriesData.clear();
            cData.upperChartSeriesTitles.clear();
            cData.upperChartSeriesXTitles.clear();
            cData.upperChartSeriesYTitles.clear();
        }
        chartsData.clear();
    }

    /**
     * @return the number of charts that this object contains
     */
    public int getChartNums() {
        return chartsData.size();
    }

    /**
     * @return an overall title for the chart item
     */
    public String getChartTitle( int num ) {
        return chartsData.get(num).chartTitle;
    }

    /**
     * @return an additional string for description
     */
    public String getChartString( int num ) {
        return chartsData.get(num).chartString;
    }

    /**
     * Set the chart title for a particular chart item number
     * 
     * @param num
     * @param chartTitle
     */
    public void setChartTitle( int num, String chartTitle ) {
        chartsData.get(num).chartTitle = chartTitle;
    }

    /**
     * Set an additional chart string for a particular chart item number
     * 
     * @param num
     * @param chartString
     */
    public void setChartString( int num, String chartString ) {
        chartsData.get(num).chartString = chartString;
    }

    public List<TwoVerticalMultiXYTimeChartItem> getChartsDataItems() {
        return chartsData;
    }

    /**
     * An item represents a separated chart instance, that will be plotted on a separated container,
     * as for example a different tab of a tabfolder
     */
    class TwoVerticalMultiXYTimeChartItem {

        private String chartTitle = ""; //$NON-NLS-1$
        private String chartString = ""; //$NON-NLS-1$

        private final List<double[][]> lowerChartSeriesData = new ArrayList<double[][]>();
        private final List<String> lowerChartSeriesTitles = new ArrayList<String>();
        private final List<String> lowerChartSeriesXTitles = new ArrayList<String>();
        private final List<String> lowerChartSeriesYTitles = new ArrayList<String>();

        private final List<double[][]> upperChartSeriesData = new ArrayList<double[][]>();
        private final List<String> upperChartSeriesTitles = new ArrayList<String>();
        private final List<String> upperChartSeriesXTitles = new ArrayList<String>();
        private final List<String> upperChartSeriesYTitles = new ArrayList<String>();

        /**
         * @return the list of double matrixes containing the two rows of data defining x-y series
         *         that define the lower chart
         */
        public List<double[][]> getLowerChartSeriesData() {
            return lowerChartSeriesData;
        }

        /**
         * @return the list of titles for the lower chart (unused, since the overall title is taken,
         *         if there are more series)
         */
        public List<String> getLowerChartSeriesTitles() {
            return lowerChartSeriesTitles;
        }

        /**
         * @return x title for the lower chart
         */
        public String getLowerChartSeriesXTitle() {
            return lowerChartSeriesXTitles.get(0);
        }

        /**
         * @return y title for the lower chart
         */
        public String getLowerChartSeriesYTitle() {
            return lowerChartSeriesYTitles.get(0);
        }

        public List<double[][]> getUpperChartSeriesData() {
            return upperChartSeriesData;
        }
        public List<String> getUpperChartSeriesTitles() {
            return upperChartSeriesTitles;
        }
        public String getUpperChartSeriesXTitle() {
            return upperChartSeriesXTitles.get(0);
        }
        public String getUpperChartSeriesYTitle() {
            return upperChartSeriesYTitles.get(0);
        }

    }

}
