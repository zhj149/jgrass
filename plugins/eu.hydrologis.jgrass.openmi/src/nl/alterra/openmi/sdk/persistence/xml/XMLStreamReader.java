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

import nl.alterra.openmi.sdk.configuration.Composition;
import nl.alterra.openmi.sdk.configuration.LinkableComponentGroup;
import nl.alterra.openmi.sdk.configuration.SystemDeployer;
import nl.alterra.openmi.sdk.configuration.Trigger;
import nl.alterra.openmi.sdk.persistence.PersistenceException;
import nl.alterra.openmi.sdk.persistence.PlaceholderComponent;
import nl.alterra.openmi.sdk.persistence.StreamReader;
import nl.alterra.openmi.sdk.backbone.*;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IOutputExchangeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Implementation of a StreamReader that handles XML formatted input.
 */
public class XMLStreamReader extends StreamReader {

    // error message(s)
    private static final String INVALID_XML_STRUCTURE_EXPECTED_TAG_S_BUT_FOUND_TAG_S =
            "Invalid XML structure. Expected tag '%s', but found tag '%s'.";
    private static final String INVALID_XML_STRUCTURE_REQUIRED_TAG_S_NOT_FOUND =
            "Invalid XML structure. Required tag '%s' not found.";
    private static final String INVALID_XML_STRUCTURE_UNSUPPORTED_ENCODING_OF_TAG_S_S_VALUE =
            "Invalid XML structure, unsupported encoding of tag''s '%s value.";
    private static final String COULD_NOT_CREATE_COMPONENT_S_S =
            "Could not create component '%s': %s";
    private static final String COULD_NOT_RECONSTRUCT_COMPONENT_S_S_REPLACED_BY_PLACEHOLDER =
            "Could not reconstruct component '%s': %s. Replaced by placeholder.";
    private static final String COULD_NOT_RECONSTRUCT_A_LINK_FROM_S_TO_S_S =
            "Could not reconstruct a link from '%s' to '%s': %s";

    /**
     * XML document being processed.
     */

    private Document doc;
    /**
     * List to record the active triggers when reading the stream.
     */

    private ArrayList<Trigger> triggers = new ArrayList<Trigger>();
    /**
     * List of warnings about component reconstruction.
     */
    
    private ArrayList<String> componentErrors = new ArrayList<String>();
    /**
     * List of warnings about link reconstruction.
     */
    
    private ArrayList<String> linkErrors = new ArrayList<String>();

    /**
     * Applies DOM based XML parser to create an internal representation of
     * the input stream.
     *
     * @throws IOException
     */
    private void parse() throws IOException {
        componentErrors.clear();
        linkErrors.clear();

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(inStream);
        }
        catch (Exception ex) {
            throw new PersistenceException(ex.getMessage());
        }
    }

    /**
     * Checks if the specified node has the specified name (ignoring case)
     * and throws an exception when it does not.
     *
     * @param aNode Node
     * @param aName String
     * @throws PersistenceException
     */
    private void forceNodeName(Node aNode, String aName) throws PersistenceException {
        if (!aNode.getNodeName().equalsIgnoreCase(aName)) {
            throw new PersistenceException(INVALID_XML_STRUCTURE_EXPECTED_TAG_S_BUT_FOUND_TAG_S, aName, aNode.getNodeName());
        }
    }

    /**
     * Finds the child node of the specified parent node that has a certain
     * tag name. If no matching node is found an exception will be thrown.
     *
     * @param aParentNode The node to search child nodes of
     * @param aName       The tag name to look for
     * @return Node found
     * @throws PersistenceException
     */
    private Node findChildNode(Node aParentNode, String aName) throws PersistenceException {
        return findChildNode(aParentNode, aName, true);
    }

    /**
     * Finds the child node of the specified parent node that has a certain
     * tag name. If no node is found and mustExist is false, null will be
     * returned. When mustExist is true and the node is not found the method
     * will throw an exception.
     *
     * @param aParentNode The node to search child nodes of
     * @param aName       The tag name to look for
     * @param mustExist   False to accept missing nodes
     * @return Node found, can be null
     * @throws PersistenceException
     */
    private Node findChildNode(Node aParentNode, String aName, boolean mustExist) throws PersistenceException {
        NodeList children = aParentNode.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equalsIgnoreCase(aName)) {
                return children.item(i);
            }
        }

        if (!mustExist) {
            return null;
        }

        throw new PersistenceException(INVALID_XML_STRUCTURE_REQUIRED_TAG_S_NOT_FOUND, aName);
    }

    /**
     * Finds the child node of the specified parent node that has a certain
     * tag name and return its (text) value. If no matching node is found an
     * exception will be thrown.
     *
     * @param aParentNode The node to search child nodes of
     * @param aName       The tag name to look for
     * @return String The value of the found node
     * @throws PersistenceException
     */
    private String findChildNodeValue(Node aParentNode, String aName) throws PersistenceException {
        return findChildNodeValue(aParentNode, aName, true);
    }

    /**
     * Finds the child node of the specified parent node that has a certain
     * tag name and return its (text) value. If no node is found and mustExist
     * is false, an empty string will be returned. When mustExist is true and
     * the node is not found the method will throw an exception.
     *
     * @param aParentNode The node to search child nodes of
     * @param aName       The tag name to look for
     * @param mustExist   False to accept missing nodes
     * @return String The value of the found node, or an empty string
     * @throws PersistenceException
     */
    private String findChildNodeValue(Node aParentNode, String aName, boolean mustExist) throws PersistenceException {
        Node n = findChildNode(aParentNode, aName, mustExist);
        if (n != null) {
            try {
                return URLDecoder.decode(n.getFirstChild().getNodeValue(), "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new PersistenceException(INVALID_XML_STRUCTURE_UNSUPPORTED_ENCODING_OF_TAG_S_S_VALUE, aName);
            }
        }
        else {
            return "";
        }
    }

    /**
     * Retrieves a new SystemDeployer from the active input connection
     * (i.e. a file or a database).
     *
     * @return SystemDeployer
     * @throws IOException
     */
    public SystemDeployer readSystemDeployer() throws IOException {
        parse();
        return readSystemDeployer(doc.getFirstChild());
    }

    /**
     * Retrieves a new Composition from the active input connection
     * (i.e. a file or a database).
     *
     * @return Composition
     * @throws IOException
     */
    public Composition readComposition() throws IOException {
        parse();
        return readComposition(doc.getFirstChild());
    }

    /**
     * Retrieves a new SystemDeployer from the specified XML node.
     *
     * @param aNode XML node to read information from
     * @return SystemDeployer
     * @throws PersistenceException
     */
    private SystemDeployer readSystemDeployer(Node aNode) throws PersistenceException {
        forceNodeName(aNode, XMLStreamTags.SYSTEM);

        SystemDeployer aSystem = new SystemDeployer(findChildNodeValue(aNode, XMLStreamTags.ID));
        aSystem.setStartTime(Double.valueOf(findChildNodeValue(aNode, XMLStreamTags.START_TIME)));
        aSystem.setEndTime(Double.valueOf(findChildNodeValue(aNode, XMLStreamTags.END_TIME)));
        aSystem.setTimeStep(Double.valueOf(findChildNodeValue(aNode, XMLStreamTags.TIME_STEP)));

        Composition composition = readComposition(findChildNode(aNode, XMLStreamTags.GROUP));
        aSystem.setComposition(composition);

        return aSystem;
    }

    /**
     * Retrieves a new Composition from the specified XML node.
     *
     * @param aNode XML node to read information from
     * @return Composition
     * @throws PersistenceException
     */
    private Composition readComposition(Node aNode) throws PersistenceException {
        return (Composition) readGroup(aNode);
    }

    /**
     * Retrieves a new LinkableComponentGroup from the specified XML node.
     *
     * @param aNode XML node to read information from
     * @return LinkableComponentGroup
     * @throws PersistenceException
     */
    private LinkableComponentGroup readGroup(Node aNode) throws PersistenceException {
        forceNodeName(aNode, XMLStreamTags.GROUP);

        LinkableComponentGroup aGroup = (LinkableComponentGroup) readLinkableComponent(aNode);

        readLinkableComponents(aGroup, findChildNode(aNode, XMLStreamTags.LINKABLE_COMPONENTS));
        readLinks(aGroup, findChildNode(aNode, XMLStreamTags.LINKS));

        return aGroup;
    }

    /**
     * Retrieves information about the links in a group from the specified
     * XML node and tries to create them in the specified group.
     *
     * @param aGroup LinkableComponentGroup to create links in
     * @param aNode  XML node to read information from
     * @throws PersistenceException
     */
    private void readLinks(LinkableComponentGroup aGroup, Node aNode) throws PersistenceException {
        Node node = aNode.getFirstChild();
        while (node != null) {
            if (node.getNodeName().equalsIgnoreCase(XMLStreamTags.LINK)) {
                // look up source and target components in the group's conmponents list:
                ILinkableComponent[] components = aGroup.getLinkableComponents();
                ILinkableComponent source,
                 target;
                source = components[Integer.valueOf(findChildNodeValue(node, XMLStreamTags.SOURCE_COMPONENT_INDEX))];
                target = components[Integer.valueOf(findChildNodeValue(node, XMLStreamTags.TARGET_COMPONENT_INDEX))];

                // get indices of the exchange items:
                int inputIndex = Integer.valueOf(findChildNodeValue(node, XMLStreamTags.TARGET_EXCHANGE_ITEM_INDEX));
                int outputindex = Integer.valueOf(findChildNodeValue(node, XMLStreamTags.SOURCE_EXCHANGE_ITEM_INDEX));

                try {
                    // the original component might be replaced by a placeholder; try to
                    // restore the links by ensuring the required number of exchange items
                    // and then to adapt the quantity and data type of the placeholder's
                    // exchange item to the other side of the link:
                    if (source instanceof PlaceholderComponent) {
                        ((PlaceholderComponent) source).ensureOutputExchangeItem(outputindex);
                    }

                    if (target instanceof PlaceholderComponent) {
                        ((PlaceholderComponent) target).ensureInputExchangeItem(inputIndex);
                    }

                    IInputExchangeItem inex = target.getInputExchangeItem(inputIndex);
                    IOutputExchangeItem outex = source.getOutputExchangeItem(outputindex);

                    if (source instanceof PlaceholderComponent) {
                        ((PlaceholderComponent) source).adaptOutputTo(outputindex, inex);
                    }

                    if (target instanceof PlaceholderComponent) {
                        ((PlaceholderComponent) target).adaptInputTo(inputIndex, outex);
                    }

                    // now try to reconnect the components; if it's not possible then
                    // silently continue and try to rebuild as much of the composition as
                    // possible:
                    Link.ValidationStatus status = Link.canConnect(source, outex, target, inex);
                    if (status != Link.ValidationStatus.Ok) {
                        throw new PersistenceException(status.getMessage());
                    }
                    else {
                        Link link = aGroup.createLink(source, outex, target, inex);
                        readBackboneObject(link, node);
                        readDataOperations(link, findChildNode(node, XMLStreamTags.DATA_OPERATIONS));
                    }
                }
                catch (Exception ex) {
                    linkErrors.add(String.format(COULD_NOT_RECONSTRUCT_A_LINK_FROM_S_TO_S_S, source, target, ex));
                }
            }
            node = node.getNextSibling();
        }
    }

    private void readBackboneObject(BackboneObject object, Node aNode) throws PersistenceException {
        object.setID(findChildNodeValue(aNode, XMLStreamTags.ID));
        object.setCaption(findChildNodeValue(aNode, XMLStreamTags.CAPTION));
        object.setDescription(findChildNodeValue(aNode, XMLStreamTags.DESCRIPTION));
    }

    /**
     * Retrieves information about data operations from the specified
     * XML node and tries to create them for the specified link.
     *
     * @param aLink Link to add data operations to
     * @param aNode XML node to read information from
     * @throws PersistenceException
     */
    private void readDataOperations(Link aLink, Node aNode) throws PersistenceException {
        Node node = aNode.getFirstChild();

        int i = 0;
        while (node != null) {
            if (node.getNodeName().equalsIgnoreCase(XMLStreamTags.DATA_OPERATION)) {
                Node argnode = findChildNode(aNode, XMLStreamTags.ARGUMENTS, false);
                if (argnode != null) {
                    aLink.getDataOperation(i).initialize(readArguments(argnode).toArray(new IArgument[]{}));
                }
            }
            node = node.getNextSibling();
            i++;
        }
    }

    /**
     * Retrieves information about components in a group from the specified
     * XML node and tries to create and add them.
     *
     * @param aGroup LinkableComponentGroup to add components to
     * @param aNode  XML node to read information from
     * @throws PersistenceException
     */
    private void readLinkableComponents(LinkableComponentGroup aGroup, Node aNode) throws PersistenceException {
        Node node = aNode.getFirstChild();

        while (node != null) {
            // detect nested groups
            if (node.getNodeName().equalsIgnoreCase(XMLStreamTags.GROUP)) {
                aGroup.addComponent(readGroup(node));
            }
            else if (node.getNodeName().equalsIgnoreCase(XMLStreamTags.LINKABLE_COMPONENT)) {
                ILinkableComponent lc = readLinkableComponent(node);
                Node trigNode = findChildNode(node, XMLStreamTags.TRIGGER, false);
                if ((lc instanceof Trigger) && (trigNode != null) && (trigNode.getAttributes() != null)) {
                    Node n = trigNode.getAttributes().getNamedItem(XMLStreamTags.ACTIVE);
                    if ((n != null) && (n.getNodeValue().equalsIgnoreCase("true"))) {
                        triggers.add((Trigger) lc);
                    }
                }
                aGroup.addComponent(lc);
            }
            node = node.getNextSibling();
        }
    }

    /**
     * Retrieves information about a linkable component from the specified
     * XML node. Based on the class path information in the node an instance
     * of the class will be created (using its <init>(String arg)) constructor
     * and its initialize() method called for the retrieved arguments. The
     * ILinkableComponent interface of the created component will be returned.
     *
     * @param aNode XML node to read information from
     * @return ILinkableComponent
     * @throws PersistenceException
     */
    private ILinkableComponent readLinkableComponent(Node aNode) throws PersistenceException {
        ILinkableComponent aComponent;

        String nameOfClass = findChildNodeValue(aNode, XMLStreamTags.CLASS_NAME);
        String id = findChildNodeValue(aNode, XMLStreamTags.INSTANCE_ID);

        // try to create an instance of the class
        try {
            Constructor c = Class.forName(nameOfClass).getConstructor(String.class);
            aComponent = (LinkableComponent) c.newInstance(id);
        }
        catch (Exception ex) {
            // on failure at the craetion of a place holder (s. below), surrender:
            if (nameOfClass.equals(PlaceholderComponent.class.getName())) {
                throw new PersistenceException(COULD_NOT_CREATE_COMPONENT_S_S, nameOfClass, ex.getMessage());
            }

            // in case there's a problem recreating the component, replace it with
            // a placeholder to conserve the composition as far as it will go:
            componentErrors.add(String.format(COULD_NOT_RECONSTRUCT_COMPONENT_S_S_REPLACED_BY_PLACEHOLDER, nameOfClass, ex));

            Node n = findChildNode(aNode, XMLStreamTags.CLASS_NAME);
            n.getFirstChild().setNodeValue(PlaceholderComponent.class.getName());
            return readLinkableComponent(aNode);
        }

        // initialize the linkable component
        Node argnode = findChildNode(aNode, XMLStreamTags.INITIALISATION_ARGUMENTS, false);
        if (argnode != null) {
            aComponent.initialize(readArguments(argnode).toArray(new IArgument[]{}));
        }

        return aComponent;
    }

    /**
     * Retrieves information from the specified XML node for an Arguments
     * collection, creates it using Argument classes and returns the result.
     *
     * @param aNode XML node to read information from
     * @return IArguments The created collection
     * @throws PersistenceException
     */
    private Arguments readArguments(Node aNode) throws PersistenceException {
        Arguments result = new Arguments();
        Node node = aNode.getFirstChild();

        while (node != null) {
            if (node.getNodeName().equalsIgnoreCase(XMLStreamTags.ARGUMENT)) {
                Argument arg = new Argument();
                arg.setKey(findChildNodeValue(node, XMLStreamTags.KEY));
                arg.setValue(findChildNodeValue(node, XMLStreamTags.VALUE));
                arg.setDescription(findChildNodeValue(node, XMLStreamTags.DESCRIPTION));
                arg.setReadOnly(findChildNodeValue(node, XMLStreamTags.READ_ONLY).equalsIgnoreCase("true"));
                result.add(arg);
            }
            node = node.getNextSibling();
        }
        return result;
    }

    /**
     * Indicates if the last read action from the IOpenMIReader interface
     * was completed successfully or not. Read methods should make an
     * attempt to restore as much as possible from the input, which can
     * result in missing links or linkable components that could not be
     * reconstructed and had to be replaced by placeholders. Where "hard"
     * errors will result in exceptions, these "soft" errors can be
     * detected by calling this method. More information can be retrieved
     * by calling getLinkErrors() and getComponentErrors().
     *
     * @return False if some kind of error or warning occurred
     */
    public boolean completedSuccesfully() {
        return ((linkErrors.size() == 0) && (componentErrors.size() == 0));
    }

    /**
     * Gets the messages related to problems with the reconstruction of
     * links on the last read action from the IOpenMIReader interface. When
     * no problems occured the returned array will have lenth 0.
     *
     * @return String[] with zero or more messages
     */
    public String[] getLinkErrors() {
        return linkErrors.toArray(new String[]{});
    }

    /**
     * Gets the messages related to problems with the reconstruction of
     * components on the last read action from the IOpenMIReader interface.
     * When no problems occured the returned array will have lenth 0.
     *
     * @return String[] with zero or more messages
     */
    public String[] getComponentErrors() {
        return componentErrors.toArray(new String[]{});
    }
    
}