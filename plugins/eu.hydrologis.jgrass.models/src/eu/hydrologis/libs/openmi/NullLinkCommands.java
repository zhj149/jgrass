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
import eu.hydrologis.libs.messages.Messages;

/**
 * <p>
 * This class contains the methods that are not used when creating a "simple model", i.e. a model
 * without linkable comonents which can be exected straight by triggering its getValues method
 * without time and link infos (null, null). A default dummy implementation is supplied where
 * needed.
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public abstract class NullLinkCommands implements ILinkableComponent {
    protected String componentDescr = null;
    protected String componentId = null;

    protected PrintStream out;
    protected PrintStream err;

    protected boolean isOkToGo = true;
    protected String userMsg = null;

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
        // if there were problems, stop and return null
        if (!isOkToGo) {
            return null;
        }

        try {
            return safeGetValues(time, linkID);
        } catch (OutOfMemoryError e) {
            err.println(Messages.getString("ModelsBackbone.outofmemory")); //$NON-NLS-1$
            JGrassModelsPlugin
                    .log(
                            "JGrassModelsPlugin problem: " + this.getClass().getCanonicalName() + "#h_netshape2flow#getValues", e); //$NON-NLS-1$ //$NON-NLS-2$
            e.printStackTrace();
            err.println();
            err.println();
            err.println();
            isOkToGo = false;
            return null;
        } catch (ModelsIllegalargumentException e) {
            err.println(e.getLocalizedMessage());
            err.println();
            err.println();
            err.println();

            isOkToGo = false;
            return null;
        } catch (Exception e) {
            err.println(e.getLocalizedMessage());
            err.println();
            err.println();
            err.println();
            JGrassModelsPlugin
                    .log(
                            "JGrassModelsPlugin problem: " + this.getClass().getCanonicalName() + "#h_netshape2flow#getValues", e); //$NON-NLS-1$ //$NON-NLS-2$
            e.printStackTrace();
            isOkToGo = false;
            return null;
        }

    }

    public abstract IValueSet safeGetValues( ITime time, String linkID ) throws Exception;

    /*
     * (non-Javadoc)
     * @see org.openmi.standard.ILinkableComponent#initialize(org.openmi.standard.IArgument[])
     */
    public void initialize( IArgument[] properties ) {

        try {
            safeInitialize(properties);
        } catch (OutOfMemoryError e) {
            err.println(Messages.getString("ModelsBackbone.outofmemory")); //$NON-NLS-1$
            JGrassModelsPlugin
                    .log(
                            "JGrassModelsPlugin problem: eu.hydrologis.jgrass.models#ModelsBackbone#initialize", e); //$NON-NLS-1$
            e.printStackTrace();
            isOkToGo = false;
        } catch (ModelsIllegalargumentException e) {
            err.println(e.getLocalizedMessage());
            isOkToGo = false;
        } catch (Exception e) {
            err.println(e.getLocalizedMessage());
            JGrassModelsPlugin
                    .log(
                            "JGrassModelsPlugin problem: eu.hydrologis.jgrass.models#ModelsBackbone#initialize", e); //$NON-NLS-1$
            e.printStackTrace();
            isOkToGo = false;
        }

    }

    public abstract void safeInitialize( IArgument[] properties ) throws Exception;

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

    public void unSubscribe( IListener arg0, EventType arg1 ) {
    }

}
