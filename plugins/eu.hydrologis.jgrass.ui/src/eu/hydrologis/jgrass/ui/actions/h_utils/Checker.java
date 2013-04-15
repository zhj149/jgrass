package eu.hydrologis.jgrass.ui.actions.h_utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IMessageManager;

public class Checker {

    private final IMessageManager mesgManager;
    private final HashMap<String, String> textsStringsMap;
    private final HashMap<String, Text> textsMap;
    private final SimpleDateFormat dateFormatter;

    public Checker( IMessageManager mesgManager, HashMap<String, String> textsStringsMap,
            HashMap<String, Text> textsMap, SimpleDateFormat dateFormatter ) {
        this.mesgManager = mesgManager;
        this.textsStringsMap = textsStringsMap;
        this.textsMap = textsMap;
        this.dateFormatter = dateFormatter;
    }

    public boolean checkString( String key, String message, int messageType ) {
        String textString = textsStringsMap.get(key);
        Text text = textsMap.get(key);
        if (textString.length() < 1) {
            mesgManager.addMessage(key, message, null, messageType, text);
            return false;
        }
        return true;
    }

    public boolean checkFileExists( String key, String message, int messageType ) {
        String textString = textsStringsMap.get(key);
        Text text = textsMap.get(key);
        File f = new File(textString);
        if (!f.exists()) {
            mesgManager.addMessage(key, message, null, messageType, text);
            return false;
        }
        return true;
    }

    public boolean checkFileWritable( String key, String message, int messageType ) {
        String textString = textsStringsMap.get(key);
        Text text = textsMap.get(key);
        File f = new File(textString);
        File parentFile = f.getParentFile();
        if (parentFile == null || !parentFile.exists() || !parentFile.canWrite()) {
            mesgManager.addMessage(key, message, null, messageType, text);
            return false;
        }
        return true;
    }

    public boolean checkIsDate( String key, String message, int messageType ) {
        String textString = textsStringsMap.get(key);
        Text text = textsMap.get(key);

        try {
            dateFormatter.parse(textString);
        } catch (ParseException e) {
            mesgManager.addMessage(key, message, null, messageType, text);
            return false;
        }
        return true;
    }

    public boolean checkIsDouble( String key, String message, int messageType ) {
        String textString = textsStringsMap.get(key);
        Text text = textsMap.get(key);

        try {
            Double.parseDouble(textString);
        } catch (NumberFormatException e) {
            mesgManager.addMessage(key, message, null, messageType, text);
            return false;
        }
        return true;
    }

    public boolean checkIsInteger( String key, String message, int messageType ) {
        String textString = textsStringsMap.get(key);
        Text text = textsMap.get(key);

        try {
            Integer.parseInt(textString);
        } catch (NumberFormatException e) {
            mesgManager.addMessage(key, message, null, messageType, text);
            return false;
        }
        return true;
    }
}
