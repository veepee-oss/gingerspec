package com.privalia.qa.lookups;

import org.apache.commons.text.lookup.StringLookup;

/**
 * Transforms the given string to upper case
 */
public class UpperCaseLookUp implements StringLookup {
    @Override
    public String lookup(String key) {
        if (key == null) {
            return null;
        }
        return key.toUpperCase();
    }
}
