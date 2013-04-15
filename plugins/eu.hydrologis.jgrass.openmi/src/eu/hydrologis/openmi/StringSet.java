package eu.hydrologis.openmi;

import nl.alterra.openmi.sdk.backbone.ValueSet;

import org.openmi.standard.IValueSet;

/**
 * Parameterized String version of the generic ValueSet.
 */
public class StringSet extends ValueSet<String> implements IValueSet {
    /**
     * Creates an empty instance.
     */
    public StringSet() {
        super();
    }

    /**
     * Creates an instance for the specified list of String objects.
     * 
     * @param values Strings to put in the collection
     */
    public StringSet( String... values ) {
        super(values);
    }

}
