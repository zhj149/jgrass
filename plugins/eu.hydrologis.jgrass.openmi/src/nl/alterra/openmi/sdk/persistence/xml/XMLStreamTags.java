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

/**
 * Shared tag names between the XML stream writer and reader.
 */
public class XMLStreamTags {

    protected static final String SYSTEM = "System";
    protected static final String ID = "ID";
    protected static final String START_TIME = "StartTime";
    protected static final String END_TIME = "EndTime";
    protected static final String TIME_STEP = "TimeStep";
    protected static final String GROUP = "Group";
    protected static final String LINKABLE_COMPONENTS = "LinkableComponents";
    protected static final String LINKABLE_COMPONENT = "LinkableComponent";
    protected static final String INITIALISATION_ARGUMENTS = "InitialisationArguments";
    protected static final String LINKS = "Links";
    protected static final String LINK = "Link";
    protected static final String SOURCE_COMPONENT_INDEX = "SourceComponentIndex";
    protected static final String SOURCE_EXCHANGE_ITEM_INDEX = "SourceExchangeItemIndex";
    protected static final String TARGET_COMPONENT_INDEX = "TargetComponentIndex";
    protected static final String TARGET_EXCHANGE_ITEM_INDEX = "TargetExchangeItemIndex";
    protected static final String DATA_OPERATIONS = "DataOperations";
    protected static final String DATA_OPERATION = "DataOperation";
    protected static final String ARGUMENTS = "Arguments";
    protected static final String INSTANCE_ID = "InstanceID";
    protected static final String CAPTION = "Caption";
    protected static final String DESCRIPTION = "Description";
    protected static final String CUSTOM_ARGUMENTS = "CustomArguments";
    protected static final String CLASS_NAME = "ClassName";
    protected static final String ARGUMENT = "Argument";
    protected static final String KEY = "Key";
    protected static final String VALUE = "Value";
    protected static final String READ_ONLY = "ReadOnly";
    protected static final String TRIGGER = "Trigger";
    protected static final String ACTIVE = "active";
    protected static final String TRIGGER_ACTIVE_B = "<Trigger active=\"%b\"/>";
    
}
