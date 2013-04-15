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
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A class representing chartable data with different teaking possibilities. This is thought to be
 * serializable to Database and should not change if possible. That is the reason for keeping all
 * the variables public.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class NumericChartData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -175455607185094027L;
    
    private NumericChartDataItem[] dataList = null;

    public NumericChartData( int tabNumber ) {
        dataList = new NumericChartDataItem[tabNumber];

        for( int i = 0; i < dataList.length; i++ ) {
            dataList[i] = new NumericChartDataItem();
        }
    }

    /**
     * Get the chart data item related to a particular "tab". This one can contain different charts.
     * Each chart can contain differen series of data.
     * 
     * @param index the number of the tab
     * @return the data item containing 1 or more charts with 1 or more series each
     */
    public NumericChartDataItem getChartDataItem( int index ) {
        if (index <= dataList.length) {
            return dataList[index];
        }
        return null;
    }

    public int getTabItemNumbers() {
        return dataList.length;
    }

    /**
     * This class represents the data and settings for a set of charts, which can contain as many
     * series as needed. The relationship to the NumericChartData is that one NumericChartData can
     * contain different tabs, which are the NumericChartDataItem.
     * 
     * @author Andrea Antonello - www.hydrologis.com
     */
    public class NumericChartDataItem implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 6781024340769991574L;
        /**
         * the title of the whole thing, this stands above all the charts
         */
        public String bigTitle = ""; //$NON-NLS-1$
        /**
         * a subtitle for the whole thing, this stands above all the charts, below the big title
         */
        public String subTitle = ""; //$NON-NLS-1$

        /**
         * a list of titles for each single chart
         */
        public List<String> chartTitles = new ArrayList<String>();

        /**
         * an additional string for the chart (can for example be used for the tabs in case the
         * title is too long)
         */
        public String chartStringExtra = ""; //$NON-NLS-1$

        /**
         * the title for the x axis for each chart
         */
        public List<String> chartXLabels = new ArrayList<String>();

        /**
         * the title for the y axis for each chart
         */
        public List<String> chartYLabels = new ArrayList<String>();

        /**
         * the name of all the series for each chart
         */
        public List<String[]> seriesNames = new ArrayList<String[]>();

        /**
         * The data objects for each chart. These represent the different series: <b>double[i][j][k]</b>
         * where:<br>
         * <ul>
         * <li>i = index for the different series</li>
         * <li>j = the X,Y rows</li>
         * <li>k = the X,Y values</li>
         * </ul>
         */
        public List<double[][][]> chartSeriesData = new ArrayList<double[][][]>();

        /**
         * Extra string annotation that can be put in a box inside the chart or somewhere. The list
         * contains an item, even if empty, for every chart. The array can contain as many
         * annotation items as needed. They are not referred to the series.
         */
        public List<String[]> annotationsInBox = new ArrayList<String[]>();

        /**
         * extra string annotation that can be put on the chart with a reference to a particular
         * coordinate shown in the chart. The list contains an item, even if empty, for every chart.
         * The hashmap can contain as many annotation items as needed. They are not referred to the
         * series.
         */
        public List<LinkedHashMap<String, double[]>> annotationsOnChart = new ArrayList<LinkedHashMap<String, double[]>>();
        
        
        /**
         * extra object that can be anything
         */
        public Object theJollyJoker = null;
    }
}
