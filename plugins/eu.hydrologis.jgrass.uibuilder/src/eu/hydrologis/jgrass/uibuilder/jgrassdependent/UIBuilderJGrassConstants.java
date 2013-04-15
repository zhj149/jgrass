package eu.hydrologis.jgrass.uibuilder.jgrassdependent;

@SuppressWarnings("nls")
public interface UIBuilderJGrassConstants {
    // main models types
    public static final String JGRASS_MODEL_ENDSPACE = "jgrass ";
    public static final String GRASS_MODEL_ENDSPACE = "grass ";
    
    // variables
    public static final String DIALOG_SIZE = "dialogsize";
    public static final String DIALOG_TITLE = "dialogtitle";
    
    
    // root tag
    public static final String COMMAND_TAG_ATTRIBUTECMDDESC = "descr";
    public static final String COMMAND_TAG_ATTRIBUTECMDNAME = "name";
    public static final String COMMAND_TAG_NAME = "command";

    // subtags
    public static final String FIELD_NAME = "field";
    public static final String FIELD_ATTRIBUTE_TYPE = "type";
    public static final String FIELD_ATTRIBUTE_REQUIRED = "required";
    public static final String FIELD_ATTRIBUTE_DEFAULT = "default";
    public static final String FIELD_ATTRIBUTE_NAME = "name";
    public static final String FIELD_ATTRIBUTE_DESCR = "desc";
    public static final String FIELD_ATTRIBUTE_ORDER = "order";
    public static final String FIELD_ATTRIBUTE_REPR = "repr";

    // field types
    public static final String FIELD_TYPE_STRING = "string";
    public static final String FIELD_TYPE_MAP = "map";
    public static final String FIELD_TYPE_INT = "int";
    public static final String FIELD_TYPE_DOUBLE = "double";
    public static final String FIELD_TYPE_FILE = "file";
    public static final String FIELD_TYPE_CHECKBOX = "check";
    public static final String FIELD_TYPE_OPTION = "option";
    public static final String FIELD_TYPE_MULTIOPTION = "multiOption";
    

}
