/* ***************************************************************************
 *
 *    Copyright (C) 2006 Alterra, Wageningen University and Research centre.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *    or look at URL www.gnu.org/licenses/lgpl.html
 *
 *****************************************************************************
 *
 * @author Rob Knapen, Alterra B.V., The Netherlands
 * @author Wim de Winter, Alterra B.V., The Netherlands
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.persistence.xml;

import nl.alterra.openmi.sdk.backbone.BackboneObject;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.configuration.Composition;
import nl.alterra.openmi.sdk.configuration.LinkableComponentGroup;
import nl.alterra.openmi.sdk.configuration.SystemDeployer;
import nl.alterra.openmi.sdk.configuration.Trigger;
import nl.alterra.openmi.sdk.persistence.StreamWriter;
import org.openmi.standard.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Implementation of a StreamWriter that produces XML formatted output.
 */
public class XMLStreamWriter extends StreamWriter {

    // error message(s)

    private static final String CAN_ONLY_WRITE_LINKS_THAT_SUPPORT_THE_ILINK_EX_INTERFACE = "Can only write links that support the ILinkEx interface!";
    /**
     * List to record the active triggers when reading the stream.
     */
    private ArrayList<Trigger> activeTriggers = new ArrayList<Trigger>();

    /**
     * Stack used for writing XML tags.
     */
    private Stack<String> tags = new Stack<String>();

    /**
     * Applies URL encoding (UTF-8) to strings to be written to the stream.
     *
     * @param str String to write
     * @throws IOException
     */
    @Override
    protected void writeToStream(String str) throws IOException {
        super.writeToStream(URLEncoder.encode(str, "UTF-8"));
    }

    /**
     * Writes the specified SystemDeployer to the active output connection
     * (i.e. a file or a database).
     *
     * @param aSystem
     * @throws IOException
     */
    public void write(SystemDeployer aSystem) throws IOException {
        tags.clear();
        writeOpenTag(XMLStreamTags.SYSTEM);

        if (aSystem != null) {
            writeTaggedValue(XMLStreamTags.ID, aSystem.getID());
            writeTaggedValue(XMLStreamTags.START_TIME, String.valueOf(aSystem.getStartTime()));
            writeTaggedValue(XMLStreamTags.END_TIME, String.valueOf(aSystem.getEndTime()));
            writeTaggedValue(XMLStreamTags.TIME_STEP, String.valueOf(aSystem.getTimeStep()));

            // save selected triggers for use when writing the composition
            activeTriggers = aSystem.getTriggers();

            write(aSystem.getComposition());
        }
        writeCloseTag();
    }

    /**
     * Writes an XML close tag to the stream that matches the last open tag
     * that was written.
     *
     * @throws IOException
     */
    private void writeCloseTag() throws IOException {
        String tag = tags.pop();
        // call super to avoid url encoding of the tags
        super.writeToStream(String.format("</%s>", tag));
    }

    /**
     * Writes an XML open tag to the stream.
     *
     * @param tag String name of the tag
     * @throws IOException
     */
    private void writeOpenTag(String tag) throws IOException {
        // call super to avoid url encoding of the tags
        super.writeToStream(String.format("<%s>", tag));
        tags.push(tag);
    }

    /**
     * Writes a tagged value to the stream. This will write an XML open tag,
     * the value, and an XML close tag.
     *
     * @param tag   The tag name
     * @param value The value to write
     * @throws IOException
     */
    private void writeTaggedValue(String tag, String value) throws IOException {
        writeOpenTag(tag);
        writeToStream(value);
        writeCloseTag();
    }

    /**
     * Writes the specified Composition to the active output connection
     * (i.e. a file or a database).
     *
     * @param aComposition
     * @throws IOException
     */
    public void write(Composition aComposition) throws IOException {
        writeGroup(aComposition);
    }

    /**
     * Writes the specified LinkableComponentGroup to the stream.
     *
     * @param aGroup LinkableComponentGroup
     * @throws IOException
     */
    private void writeGroup(LinkableComponentGroup aGroup) throws IOException {
        writeOpenTag(XMLStreamTags.GROUP);

        if (aGroup != null) {
            writeClassName(aGroup);
            writeComponentProperties(aGroup);
            writeComponentsToStream(aGroup);
            writeLinksToStream(aGroup);
        }
        writeCloseTag();
    }

    /**
     * Writes the linkable components from a LinkableComponentGroup to the
     * stream.
     *
     * @param aGroup LinkableComponentGroup
     * @throws IOException
     */
    private void writeComponentsToStream(LinkableComponentGroup aGroup)
            throws IOException {
        writeOpenTag(XMLStreamTags.LINKABLE_COMPONENTS);

        for (ILinkableComponent lc : aGroup.getLinkableComponents()) {
            if (lc instanceof LinkableComponentGroup) {
                writeGroup((LinkableComponentGroup) (lc));
            }
            else {
                writeOpenTag(XMLStreamTags.LINKABLE_COMPONENT);
                writeClassName(lc);

                if (lc instanceof Trigger) {
                    super.writeToStream(String.format(XMLStreamTags.TRIGGER_ACTIVE_B, activeTriggers.contains((Trigger) lc)));
                }

                writeComponentProperties(lc);
                writeArgumentsToStream(XMLStreamTags.INITIALISATION_ARGUMENTS, null);

                writeCloseTag();
            }
        }
        writeCloseTag();
    }

    /**
     * Writes the links from a LinkableComponentGroup to the stream.
     *
     * @param aGroup LinkableComponentGroup
     * @throws IOException
     */
    private void writeLinksToStream(LinkableComponentGroup aGroup)
            throws IOException {
        writeOpenTag(XMLStreamTags.LINKS);

        for (ILink link : aGroup.getLinks()) {
            writeLinkToStream(aGroup, link);
        }
        writeCloseTag();
    }

    /**
     * Writes a link to the stream. It must support the ILinkV2 interface so
     * that references to exchange items can be saved.
     *
     * @param aGroup The LinkableComponentGroup the link is part of
     * @param link   ILink to write
     * @throws IOException
     */
    private void writeLinkToStream(LinkableComponentGroup aGroup, ILink link)
            throws IOException {

        writeOpenTag(XMLStreamTags.LINK);
        writeBackboneObject((Link) link);

        writeTaggedValue(XMLStreamTags.SOURCE_COMPONENT_INDEX,
                String.valueOf(aGroup.getIndexOfComponent(link.getSourceComponent())));
        writeTaggedValue(XMLStreamTags.SOURCE_EXCHANGE_ITEM_INDEX,
                String.valueOf(getIndexOfExchangeItem(link.getSourceComponent(), link.getSourceQuantity(), link.getSourceElementSet())));
        writeTaggedValue(XMLStreamTags.TARGET_COMPONENT_INDEX,
                String.valueOf(aGroup.getIndexOfComponent(link.getTargetComponent())));
        writeTaggedValue(XMLStreamTags.TARGET_EXCHANGE_ITEM_INDEX,
                String.valueOf(getIndexOfExchangeItem(link.getTargetComponent(), link.getTargetQuantity(), link.getTargetElementSet())));

        writeDataOperationsToStream(link);

        writeCloseTag();    // link
    }

    /**
     * Writes the generic data of a backbone object to the stream.
     *
     * @param object Backbone object
     * @throws IOException
     */
    private void writeBackboneObject(BackboneObject object) throws IOException {
        writeTaggedValue(XMLStreamTags.ID, object.getID());
        writeTaggedValue(XMLStreamTags.CAPTION, object.getCaption());
        writeTaggedValue(XMLStreamTags.DESCRIPTION, object.getDescription());
    }

    /**
     * Writes information about the data operations of a link to the stream.
     *
     * @param link ILink to write data operation information for
     * @throws IOException
     */
    private void writeDataOperationsToStream(ILink link)
            throws IOException {
        writeOpenTag(XMLStreamTags.DATA_OPERATIONS);
        for (int i = 0; i < link.getDataOperationsCount(); i++) {
            writeOpenTag(XMLStreamTags.DATA_OPERATION);
            writeToStream((link.getDataOperation(i).getID()));

            writeOpenTag(XMLStreamTags.ARGUMENTS);
            for (int j = 0; j < link.getDataOperation(i).getArgumentCount(); j++) {
                writeArgumentToStream(link.getDataOperation(i).getArgument(j));
            }
            writeCloseTag();
            writeCloseTag();
        }
        writeCloseTag();
    }

    /**
     * Writes common linkable component properties to the stream.
     *
     * @param lc
     * @throws IOException
     */
    private void writeComponentProperties(ILinkableComponent lc)
            throws IOException {
        writeTaggedValue(XMLStreamTags.INSTANCE_ID, lc.getModelID());
        writeTaggedValue(XMLStreamTags.CAPTION, lc.getModelID());
        writeTaggedValue(XMLStreamTags.DESCRIPTION, lc.getModelDescription());
        writeArgumentsToStream(XMLStreamTags.CUSTOM_ARGUMENTS, null);
    }

    /**
     * Writes the class name of the ILinkableComponent to the stream.
     *
     * @param lc ILinkableComponent
     * @throws IOException
     */
    private void writeClassName(ILinkableComponent lc)
            throws IOException {
        writeTaggedValue(XMLStreamTags.CLASS_NAME, lc.getClass().getName());
    }

    /**
     * Writes information about arguments to the stream.
     *
     * @param tag  Tag to use
     * @param args IArguments to write
     * @throws IOException
     */
    private void writeArgumentsToStream(String tag, List<IArgument> args) throws IOException {
        writeOpenTag(tag);
        for (IArgument arg : args) {
            writeArgumentToStream(arg);
        }
        writeCloseTag();
    }

    /**
     * Writes information about an IArgument to the stream.
     *
     * @param arg IArgument to write
     * @throws IOException
     */
    private void writeArgumentToStream(IArgument arg) throws IOException {
        writeOpenTag(XMLStreamTags.ARGUMENT);

        writeTaggedValue(XMLStreamTags.KEY, arg.getKey());
        writeTaggedValue(XMLStreamTags.VALUE, arg.getValue());
        writeTaggedValue(XMLStreamTags.READ_ONLY, String.valueOf(arg.isReadOnly()));
        writeTaggedValue(XMLStreamTags.DESCRIPTION, arg.getDescription());

        writeCloseTag();
    }

    /**
     * Gets the index of an IInputExchangeItem in a ILinkableComponent.
     *
     * @param component ILinkableComponent
     * @param item      IInputExchangeItem
     * @return int Index, -1 when not found
     */
    private int getIndexOfInputExchangeItem(ILinkableComponent component, IInputExchangeItem item) {
        for (int i = 0; i < component.getInputExchangeItemCount(); i++) {
            if (component.getInputExchangeItem(i) == item) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the index of an IOutputExchangeItem in a ILinkableComponent.
     *
     * @param component ILinkableComponent
     * @param quantity
     * @param elementSet
     * @return int Index, -1 when not found
     */
    private int getIndexOfExchangeItem(ILinkableComponent component, IQuantity quantity, IElementSet elementSet) {
        for (int i = 0; i < component.getOutputExchangeItemCount(); i++) {
            if (component.getOutputExchangeItem(i).getQuantity().equals(quantity) &&
                    component.getOutputExchangeItem(i).getElementSet().equals(elementSet)) {
                return i;
            }
        }
        return -1;
    }

}