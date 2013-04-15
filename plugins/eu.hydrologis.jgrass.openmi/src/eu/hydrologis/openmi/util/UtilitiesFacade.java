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
package eu.hydrologis.openmi.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import nl.alterra.openmi.sdk.backbone.Dimension;
import nl.alterra.openmi.sdk.backbone.InputExchangeItem;
import nl.alterra.openmi.sdk.backbone.OutputExchangeItem;
import nl.alterra.openmi.sdk.backbone.Quantity;
import nl.alterra.openmi.sdk.backbone.Unit;

import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.IDimension.DimensionBase;

public class UtilitiesFacade {

    public static IQuantity createScalarQuantity( String quantityId, String unitId ) {
        /*
         * create a dimension obj
         */
        Dimension dimension = new Dimension();

        double convToSi = 1.0;
        String unitdescr = unitId;
        if (unitId.equals("mm/h")) {
            convToSi = 1.0 / 3600.0 / 1000.0;
            unitdescr = "millimeters per hour";
            dimension.setPower(DimensionBase.Length, 1);
            dimension.setPower(DimensionBase.Time, -1);
        } else if (unitId.equals("m3/s")) {
            convToSi = 1.0;
            unitdescr = "cubic meters per second";
            dimension.setPower(DimensionBase.Length, 3);
            dimension.setPower(DimensionBase.Time, -1);
        } else if (unitId.equals("mm3/h")) {
            convToSi = 1.0 / 3600.0 / 1E9;
            unitdescr = "cubic millimeters per hour";
            dimension.setPower(DimensionBase.Length, 3);
            dimension.setPower(DimensionBase.Time, -1);
        } else {
            convToSi = 1;
            unitdescr = unitId;
            dimension.setPower(DimensionBase.Length, 1);
            dimension.setPower(DimensionBase.Time, 1);
        }

        Unit unit = new Unit(unitId, // ID
                convToSi, // conv. to SI
                0.0, // offset to SI
                unitdescr // description
        );

        /*
         * create the quantity
         */
        IQuantity quantity = new Quantity(unit, quantityId, quantityId, IQuantity.ValueType.Scalar,
                dimension);
        return quantity;
    }
    public static IOutputExchangeItem createOutputExchangeItem( ILinkableComponent owner,
            IQuantity quantity, IElementSet elementSet ) {
        /*
         * create the outputexchangeitem (here we have no inputexchangeitem)
         */
        OutputExchangeItem outputExchangeItem = new OutputExchangeItem(owner, quantity.getID()
                + "/" + elementSet.getID());
        outputExchangeItem.setQuantity(quantity);
        outputExchangeItem.setElementSet(elementSet);
        return outputExchangeItem;
    }

    public static IInputExchangeItem createInputExchangeItem( ILinkableComponent owner,
            IQuantity quantity, IElementSet elementSet ) {
        /*
         * create the outputexchangeitem (here we have no inputexchangeitem)
         */
        InputExchangeItem inputExchangeItem = new InputExchangeItem(owner, quantity.getID() + "/"
                + elementSet.getID());
        inputExchangeItem.setQuantity(quantity);
        inputExchangeItem.setElementSet(elementSet);
        return inputExchangeItem;
    }
    
    public static double[] convertToTargetUnit( ILink link, double[] values ) {
        double[] convertedValues = new double[values.length];
        double conversionFactor = link.getSourceQuantity().getUnit().getConversionFactorToSI()
                / link.getTargetQuantity().getUnit().getConversionFactorToSI();

        if (conversionFactor == 1.0)
            return values;

        // TODO incorporate offset
        for( int i = 0; i < values.length; i++ ) {
            convertedValues[i] = values[i] * conversionFactor;
        }
        System.out.println(link.getSourceComponent().getModelID()
                + ": returning converted values for " + link.getID() + ": " + convertedValues[0]
                + " from: " + link.getSourceQuantity().getUnit().getID() + " to "
                + link.getTargetQuantity().getUnit().getID());

        return convertedValues;
    }

    /**
     * Transforms a date and time string of the format:<br>
     * <i>dd/mm/yyyy hh:mm:ss</i><br>
     * to a calendar object.
     * 
     * @param dateAndTime the date and time string to convert
     * @return the generated calendar object
     */
    public static Calendar calenderFromDateString( String dateAndTime ) {
        String[] dateTime = dateAndTime.trim().split(" ");
        String datetoken = dateTime[0];
        String timetoken = dateTime[1];
        String[] startDay = datetoken.trim().split("/");
        String[] startHour = timetoken.trim().split(":");
        return new GregorianCalendar(Integer.parseInt(startDay[2]),
                Integer.parseInt(startDay[1]) - 1, Integer.parseInt(startDay[0]), Integer
                        .parseInt(startHour[0]), Integer.parseInt(startHour[1]), Integer
                        .parseInt(startHour[2]));
    }

    /**
     * Transforms a date and time string of the format:<br>
     * <i>dd/mm/yyyy hh:mm:ss</i><br>
     * to a double julianday
     * 
     * @param dateAndTime the date and time string to convert
     * @return the generated julian day
     */
    // public static double modifiedJulianDayFromDateString(String dateAndTime) {
    // double timeInFileMJD = CalendarConverter
    // .gregorian2ModifiedJulian(calenderFromDateString(dateAndTime));
    // return timeInFileMJD;
    // }
    /**
     * Checks wether the calendar time is >= than the running time.
     * 
     * @param julianDay - running time
     * @param timeInFileModifiedJulian - file time expressed in double
     * @return false if the file time is < runningtime
     */
    public static boolean requestedTimeLaterThen( double julianDay, double timeInFileModifiedJulian ) {
        if (timeInFileModifiedJulian == -9999.0) {
            return true;
        }
        /*
         * TODO check if the date is passed exactly on the asked time moment. A consistent model
         * should do some good checking and in case interpolation. For demonstration purposes this
         * is enough.
         */
        // System.out.println("Comparing " + timeInFileModifiedJulian + " < "
        // + julianDay + " diff = "
        // + Math.abs(timeInFileModifiedJulian - julianDay));
        if (timeInFileModifiedJulian < julianDay
                && (Math.abs(timeInFileModifiedJulian - julianDay) < 0.00001)) {
            // System.out.println("FALSE");
            return false;
        }
        if (timeInFileModifiedJulian < julianDay) {
            // System.out.println("TRUE");
            return true;
        }

        // System.out.println("FALSE");
        return false;
    }

    public static double roundMjdToSeconds( double timeStep ) {
        double jd0 = timeStep * 100000.0;
        double jd = Math.floor(jd0);
        if (jd0 - jd > 0.5)
            ++jd;
        timeStep = jd / 100000;
        return timeStep;
    }

    public static double[][] arrayToMatrix( int rows, int cols, double[] array ) {
        int index = 0;
        double[][] elevation = new double[rows][cols];
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                elevation[i][j] = array[index];
                index++;
            }
        }
        return elevation;
    }

    public static double[] matrixToArray( double[][] matrix ) {
        int index = 0;
        int rows = matrix.length;
        int cols = matrix[0].length;

        double[] array = new double[rows * cols];
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                array[index] = matrix[i][j];
                index++;
            }
        }
        return array;
    }

    // public static void main(String[] args) {
    //
    // /*
    // * this is done with a calendar instance
    // */
    // String dateAndTime = "10/08/2006 00:15:00";
    // String[] dateTime = dateAndTime.trim().split(" ");
    // String datetoken = dateTime[0];
    // String timetoken = dateTime[1];
    // String[] startDay = datetoken.trim().split("/");
    // String[] startHour = timetoken.trim().split(":");
    // double timeFromDateString = CalendarConverter
    // .gregorian2ModifiedJulian(new GregorianCalendar(Integer
    // .parseInt(startDay[2]),
    // Integer.parseInt(startDay[1]) - 1, Integer
    // .parseInt(startDay[0]), Integer
    // .parseInt(startHour[0]), Integer
    // .parseInt(startHour[1]), Integer
    // .parseInt(startHour[2])));
    //
    // /*
    // * this is done through the timestamp utility and adding a timestep
    // */
    // double timeStep = Double.valueOf(900.0) / 86400.0d;
    //
    // double curTime = 53926.0d;
    // TimeStamp beginTimeStamp = new TimeStamp(curTime);
    // double curTimeAsMJD = beginTimeStamp.getModifiedJulianDay() + timeStep;
    //
    // /*
    // * The time is the same, but is not the same.
    // *
    // * I think you guys have some rounding error lying around...
    // */
    // System.out.println(timeFromDateString + " " + curTimeAsMJD);
    // }

}
