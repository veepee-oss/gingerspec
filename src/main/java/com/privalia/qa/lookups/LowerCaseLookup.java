package com.privalia.qa.lookups;

import org.apache.commons.text.lookup.StringLookup;

/**
 * Transforms the given string to lower case
 */
public class LowerCaseLookup implements StringLookup {
    @Override
    public String lookup(String key) {
        if (key == null) {
            return null;
        }
        return key.toLowerCase();
    }
}
