package eu.hydrologis.jgrass.uibuilder;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.NodeList;

import eu.hydrologis.jgrass.uibuilder.jgrassdependent.GuiBuilderDialog;
import eu.hydrologis.jgrass.uibuilder.jgrassdependent.UIBuilderJGrassConstants;
import eu.hydrologis.jgrass.uibuilder.jgrassdependent.XmlCreator;

public class DynamicExample {
    @SuppressWarnings("nls")
    public static void main( String[] args ) {
        Display display = new Display();
        Shell shell = new Shell(display, SWT.DIALOG_TRIM);
        shell.setText("SWT Test");
        Properties properties = new Properties();

        // r.in.gdal -o -e input=c:/asdasd output=asdout location=asdloc --overwrite
        XmlCreator xmlCreator = new XmlCreator("r.in.gdal", "r.in.gdal descr");
        xmlCreator.addFileChooserItem("input", "map to import", "input=#", "", true);
        xmlCreator.addLabeledStringItem("output", "output map name", "output=#", true);
        xmlCreator.addLabeledStringItem("location", "location to create", "location=#", false);
        xmlCreator.addCheckBoxItem("map overwrite", "permit overwrite", "--overwrites", true);
        xmlCreator.addCheckBoxItem("projection overwrite", "override projection", "-o", false);
        xmlCreator.addCheckBoxItem("extend region", "extend region with imported map?", "-e", true);

        try {
            xmlCreator.dumpXmlToFile(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        properties.put(UIBuilderJGrassConstants.DIALOG_SIZE, new Point(400, 200));
        properties.put(UIBuilderJGrassConstants.DIALOG_TITLE, "bau");
        NodeList nodeList = xmlCreator.getNodeList();
        GuiBuilderDialog swtDialog = new GuiBuilderDialog(shell, nodeList, properties, false, false);

        swtDialog.setBlockOnOpen(true);
        swtDialog.open();
        System.out.println(properties.get("CommandLine"));
    }
}
