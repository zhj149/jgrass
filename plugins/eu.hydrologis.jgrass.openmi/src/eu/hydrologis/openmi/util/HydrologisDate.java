/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) { 
 * HydroloGIS - www.hydrologis.com                                                   
 * C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam                               
 * The JGrass developer team - www.jgrass.org                                         
 * }
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.openmi.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmi.standard.ITime;

/**
 * The time object in use in JGrass to implerment openmi models.
 * 
 * <p>
 * This class is needed to deal with time in openMi. The getValues wants an ITime argument, so we
 * supply our own, but we want to use the java date and time engine.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class HydrologisDate extends Date implements ITime {

    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;

    private DateFormat dbFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DateFormat printableFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");

    /*
     * nothing needed here, we just want to use java time engine
     */

    public HydrologisDate() {
        super();
    }

    public HydrologisDate( long time ) {
        super(time);
    }

    /**
     * Returns the date formatted with standard database format.
     * 
     * <p>The format is: yyyy-MM-dd HH:mm:ss</p>
     * 
     * @return the formatted date string.
     */
    public String getDbFormat() {
        return dbFormatter.format(this);
    }

    /**
     * Returns the date formatted to be usable for filenames.
     * 
     * <p>The format is without spaces and strange symbols: yyyy-MM-dd_HH-mm</p>
     * 
     * @return the formatted date string.
     */
    public String getPrintableFormat() {
        return printableFormatter.format(this);
    }
}
