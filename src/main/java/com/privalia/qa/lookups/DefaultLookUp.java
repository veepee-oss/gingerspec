package com.privalia.qa.lookups;

import com.privalia.qa.utils.ThreadProperty;
import org.apache.commons.text.lookup.StringLookup;

/**
 * Default String Lookup used during variable replacement.
 */
public class DefaultLookUp implements StringLookup {

    /**
     * Search the given property within the Thread properties first, it not found,
     * it will try to locate the property in the System properties.
     * @param key   key to look for
     * @return      the value for the given key
     */
    @Override
    public String lookup(String key) {
        if (key == null) {
            return null;
        }
        if (ThreadProperty.get(key) != null) {
            return ThreadProperty.get(key);
        }
        return System.getProperty(key);
    }
}
