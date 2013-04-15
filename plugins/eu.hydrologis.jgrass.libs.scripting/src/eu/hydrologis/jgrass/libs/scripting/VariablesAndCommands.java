package eu.hydrologis.jgrass.libs.scripting;

import java.util.ArrayList;
import java.util.List;

/**
 * Vars added to eu.hydrologis.jgrass.console.core.prefs.Projectspace
 * for syntax coloring.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class VariablesAndCommands {

    public static List<String> variables = new ArrayList<String>();
    public static List<String> commands = new ArrayList<String>();
    
    static {
        variables.add("NORTH");
        variables.add("SOUTH");
        variables.add("EAST");
        variables.add("WEST");
        variables.add("WERES");
        variables.add("NSRES");
        variables.add("ROWS");
        variables.add("COLS");
        variables.add("ACTIVEREGION");
        variables.add("DB");
        
        commands.add("help");
        commands.add("coordinateFromRowCol");
        commands.add("rowColFromCoordinate");
        commands.add("printMap");
        commands.add("dataset");
        commands.add("select");
        commands.add("count");
        commands.add("matrixToGrassMap");
        commands.add("grassMapToMatrix");
        commands.add("valueOfGrassMapInCoordinate");
        commands.add("getFeaturesFromLayer");
        commands.add("getFilteredFeaturesFromLayer");
        commands.add("getFeaturesFromShapefile");
        commands.add("getFilteredFeaturesFromShapefile");
        commands.add("loadGrassMap");
        commands.add("grassMapAvg");
        commands.add("grassMapMin");
        commands.add("grassMapMax");
        commands.add("grassMapModa");
        commands.add("featuresLayerAvg");
        commands.add("shapefileAvg");
        commands.add("featuresLayerMin");
        commands.add("shapefileMin");
        commands.add("featuresLayerMax");
        commands.add("shapefileMax");
        commands.add("featuresLayerModa");
        commands.add("shapefileModa");
        commands.add("deleteGrassMap");
        commands.add("copyToNewMapset");
        commands.add("exportEsriAscii");
        commands.add("exportTiff");
        commands.add("importEsriAscii");
        commands.add("importTiff");
        commands.add("mapcalc");
    }
}
