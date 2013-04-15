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
package eu.hydrologis.libs.openmi;

import java.io.File;
import java.io.PrintStream;

import org.openmi.standard.IArgument;
import org.openmi.standard.IEvent;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IListener;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.ITimeSpan;
import org.openmi.standard.ITimeStamp;
import org.openmi.standard.IValueSet;
import org.openmi.standard.IEvent.EventType;

import eu.hydrologis.JGrassModelsPlugin;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;

/**
 * <p>
 * This class contains the methods that are not used when you do not really need to link components
 * the openmi way. This should delegate to some easy operation. A default dummy implementation is
 * supplied for the mandatory methods.
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public abstract class StraightExecutableCommands implements ILinkableComponent {
    protected String componentDescr = null;
    protected String componentId = null;
    protected PrintStream out;
    protected PrintStream err;

    protected String mapName = null;
    protected String mapset = null;
    protected String locationPath = null;
    protected JGrassRegion activeRegion = null;

    public void safeInitialize( IArgument[] properties ) throws Exception{

        String grassDb = null;
        String location = null;
        if (properties != null) {
            for( IArgument argument : properties ) {
                String key = argument.getKey();
                if (key.compareTo("grassdb") == 0) {
                    grassDb = argument.getValue();
                }
                if (key.compareTo("location") == 0) {
                    location = argument.getValue();
                }
                if (key.compareTo("mapset") == 0) {
                    mapset = argument.getValue();
                }
                if (key.compareTo(ModelsConstants.DEFAULTKEY) == 0) {
                    mapName = argument.getValue();
                }
            }

        }
        locationPath = grassDb + File.separator + location;

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + "WIND";
        activeRegion = new JGrassRegion(activeRegionPath);

    }

    public String getComponentDescription() {
        return componentDescr;
    }

    public String getComponentID() {
        return componentId;
    }

    public String getModelDescription() {
        return componentDescr;
    }

    public String getModelID() {
        return componentId;
    }

    public void addLink( ILink link ) {
    }

    public void dispose() {
    }

    public void finish() {
    }

    public ITimeStamp getEarliestInputTime() {
        return null;
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return null;
    }

    public int getInputExchangeItemCount() {
        return 0;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 0;
    }

    public ITimeSpan getTimeHorizon() {
        return null;
    }

    public IValueSet getValues( ITime time, String linkID ) {
        try {
            return safeGetValues(time, linkID);
        } catch (OutOfMemoryError e) {
            err
                    .println("JGrass went out of memory, consider to supply more momory to the process.");
            JGrassModelsPlugin
                    .log(
                            "JGrassModelsPlugin problem: " + this.getClass().getCanonicalName() + "#h_netshape2flow#getValues", e); //$NON-NLS-1$
            e.printStackTrace();
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public abstract IValueSet safeGetValues( ITime time, String linkID ) throws Exception;

    public void prepare() {
    }

    public void removeLink( String linkID ) {
    }

    public String validate() {
        return null;
    }

    public EventType getPublishedEventType( int providedEventTypeIndex ) {
        return null;
    }

    public int getPublishedEventTypeCount() {
        return 0;
    }

    public void sendEvent( IEvent Event ) {
    }

    public void subscribe( IListener listener, EventType eventType ) {
    }

    public void unsubscribe( IListener listener, EventType eventType ) {
    }

}
