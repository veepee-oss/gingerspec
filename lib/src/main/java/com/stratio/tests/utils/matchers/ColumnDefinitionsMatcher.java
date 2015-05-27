package com.stratio.tests.utils.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.Factory;
/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 *
 */
public class ColumnDefinitionsMatcher extends TypeSafeMatcher<String> {

    private final String columnName;

    /**
     * Constructor.
     * @param columnName
     */
    public ColumnDefinitionsMatcher(String columnName) {
        this.columnName = columnName;
    }
    
    /**
     * Matcher contains column.
     * @param columnName
     * @return Matcher<String>
     */
    @Factory
    public static Matcher<String> containsColumn(String columnName) {
        return new ColumnDefinitionsMatcher(columnName);
    }

    @Override
    protected boolean matchesSafely(String item) {
        String[] aux = item.split("\\p{Punct}");
        for (int i = 0; i < aux.length; i++) {
            aux[i] = aux[i].trim();
            if (aux[i].equals(this.columnName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("The column " + this.columnName + " does not exists on the table.");
    }
}
