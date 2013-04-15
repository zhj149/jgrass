#! /usr/bin/env groovy
/*
 * this script is intended to create the necessary structure 
 * for adding guys to new commands. It parses the files of the 
 * console engine: nativegrass.xml and standard_linkables.xml and 
 * extracts all the command.
 * 
 * What it does for you:
 * 1) create the needed action class to launch the gui (taking into account the difference between native and java)
 * 2) add the needed entry in the rcp plugin system to make the action appear in the menu entries
 * 
 * What you need to add:
 * 1) at the same level of the created class an xml file describing the command
 */

///////////////////////////////////////////////////////////////////////
//  IN THIS PART THE USER CAN CHANGE THINGS 
///////////////////////////////////////////////////////////////////////
boolean doJgrass = true;
boolean doGrass = false;
// some standard variables
def nativeCommandsPath = "./nativegrass.xml";
def jgrassCommandsPath = "./standard_linkables.xml";
def actionsFolder = "../src/eu/hydrologis/jgrass/ui/actions/"

def grassCommandsFilter = [r:"raster", g:"generic", v:"vector"];



///////////////////////////////////////////////////////////////////////
//  FROM THIS PART ON, THE USER SHOULD NOT CHANGE 
///////////////////////////////////////////////////////////////////////

// parse the command files
/*
 * jgrass commands
 */
if(doJgrass) {
	def jgrassCommandsText = new File(jgrassCommandsPath).text;
	// slurper needs a root element, dynamically create one
	jgrassCommandsText = """<document>""" + jgrassCommandsText + """</document>"""
	def records = new XmlSlurper().parseText(jgrassCommandsText);
	
	def jgComList = records.children();
	// filter out only commands that are models, i.e. have some dot inside
	jgComList = jgComList.findAll{
	    it.@name.toString().indexOf('.') != -1
	}
	def jgCommandNames = new ArrayList();
	jgComList.@name.each{
	    jgCommandNames.add(it.toString().replaceAll("\\.","_"));
	}
	// if it doesn't exist already, create a class file for the command 
	for (int i = 0; i < jgCommandNames.size(); i++) {
	    String cmd = jgCommandNames.get(i);
	    String outFile = actionsFolder + cmd + ".java";
	    File f = new File(outFile);
	    if (!f.exists()) {
	        // create it
	        commandClass = """/*
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
package eu.hydrologis.jgrass.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import eu.hydrologis.jgrass.uibuilder.jgrassdependent.utils.UIBuilderActionSupporter;

/**
 * this class was generated by JGrass's createCommands script
 */
public class ${cmd} extends UIBuilderActionSupporter implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
    }

    public void run( IAction action ) {
        launchGui(window);
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }
}
"""
	           f.write(commandClass);
	    }else{
	        println "Not creating file: " + outFile
	    }
	}
}

/*
 * grass commands, here also the plugin menu part is created
 */
if(doGrass) {
	def grassCommandsText = new File(nativeCommandsPath).text;
	records = new XmlSlurper().parseText(grassCommandsText);
	
	def gComList = records.children();
	//filter out only commands that are models, i.e. have some dot inside
	gComList = gComList.findAll{
	    it.@name.toString().indexOf('.') != -1
	}
	def gCommandNames = new ArrayList();
	gComList.@name.each{ comm ->
	    // filter 
	    grassCommandsFilter.each{k, v ->
	        if(comm.toString().startsWith(k + ".")){
	            gCommandNames.add(comm.toString().replaceAll("\\.","_"));
	        }
	    }
	}
	// create menu entry for different types of grass commands
	
	def pluginXml1 = """
		<actionSet
		    description="Native GRASS commands"
		    id="eu.hydrologis.jgrass.ui.grassactionset"
		    label="GRASS" visible="true">
		    <menu id="grass" label="GRASS">
		    <separator
		          name="grassGroupSep">
		    </separator>
		    <groupMarker
		          name="grassGroup">
		    </groupMarker>
		    </menu>
		"""
	def plugnXmlString = pluginXml1;
	grassCommandsFilter.each{k, v ->
		def menuPart = """
		    <menu
		       id="${v}Menu"
		       label="${v}"
		       path="grass/${v}Group">
		    <groupMarker
		          name="${v}MenuGroup">
		    </groupMarker>
		    </menu>
		 """
		plugnXmlString =  plugnXmlString + menuPart;
	}
		
	//if it doesn't exist already, create a class file for the command 
	for (int i = 0; i < gCommandNames.size(); i++) {
	    String cmd = gCommandNames.get(i);
	    String outFile = actionsFolder + cmd + ".java";
	    File f = new File(outFile);
	    if (!f.exists()) {
	        // create it
	        commandClass = """/*
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
package eu.hydrologis.jgrass.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import eu.hydrologis.jgrass.uibuilder.jgrassdependent.utils.UIBuilderActionSupporter;

/**
 * this class was generated by JGrass's createCommands script
 */
public class ${cmd} extends UIBuilderActionSupporter implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
    }

    public void run( IAction action ) {
        isGrass = true;
        launchGui(window);
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }
}
"""
	           f.write(commandClass);
	    }else{
	        println "Not creating file: " + outFile
	    }
	    
	    // and create the action entry
	    String type = grassCommandsFilter.get(cmd[0]);
	    
	    def icon = "";
	    File icn = new File("icons/grass/" + cmd + ".png")
	    if(icn.exists())
	    {
			 def actionPart = """
			 <action
			       class="eu.hydrologis.jgrass.ui.actions.${cmd}"
			       icon ="icons/grass/${cmd}.png"
			       id="eu.hydrologis.jgrass.ui.actions.${cmd}"
			       label="${cmd}"
			       menubarPath="grass/${type}Menu/${type}MenuGroup"
			       tooltip="${cmd}">
			 </action>
			"""
			plugnXmlString = plugnXmlString + actionPart;
	    } else {
	    	def dotCmd = cmd.replaceAll("_",".");
			 def actionPart = """
			 <action
			       class="eu.hydrologis.jgrass.ui.actions.${cmd}"
			       id="eu.hydrologis.jgrass.ui.actions.${cmd}"
			       label="${dotCmd}"
			       menubarPath="grass/${type}Menu/${type}MenuGroup"
			       tooltip="${dotCmd}">
			 </action>
			"""
			plugnXmlString = plugnXmlString + actionPart;
	    }
	}
	def endPart = """
	</actionSet>
	"""
	plugnXmlString = plugnXmlString + endPart;
	
	
	File f = new File("pluginXml.xml")
	f.write(plugnXmlString)

}









