package com.stratio.tests.utils.matchers;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Record;
import com.aerospike.client.query.RecordSet;

import cucumber.api.DataTable;

public class RecordSetMatcher extends TypeSafeMatcher<RecordSet> {
    private final DataTable table;

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordSetMatcher.class);

    /**
     * Default constructor.
     * @param table
     */
    public RecordSetMatcher(DataTable table) {
        this.table = table;
    }
    /**
     * Checks if a dataTable is contained in a recordSet.
     * @param table
     */
    @Factory
    public static RecordSetMatcher containedInRecordSet(DataTable table) {
        return new RecordSetMatcher(table);
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("The data of the table is not contained in the RecordSet");
    }

    @Override
    protected boolean matchesSafely(RecordSet item) {
        List<List<String>> tableAsList = table.raw();
        List<String> columnNames = tableAsList.get(0);
        List<List<String>> recordsAsTable = recordToList(columnNames, item);
        // Primero comprobamos las longitudes(han de ser las mismas)
        if (recordsAsTable.size() != tableAsList.size()) {
            return false;
        }
        // Pasamos a comprobar el contenido del recordset por filas
        for (int i = 1; i < tableAsList.size(); i++) {
            if (recordsAsTable.contains(tableAsList.get(i))) {
                recordsAsTable.remove(tableAsList.get(i));
            } else {
                return false;
            }
        }
        return true;
    }

    private List<List<String>> recordToList(List<String> columnNames, RecordSet item) {
        List<List<String>> recordsAsList = new ArrayList<List<String>>();
        recordsAsList.add(columnNames);
        try {
            try {
                while (item.next()) {
                    Record record = item.getRecord();
                    List<String> recordAsList = new ArrayList<String>();
                    for (int i = 0; i < columnNames.size(); i++) {
                        Object aux = record.getValue(columnNames.get(i));
                        if (aux != null) {
                            recordAsList.add(aux.toString());
                        } else {
                            recordAsList.add("ERROR");
                        }
                    }
                    recordsAsList.add(recordAsList);
                }
            } catch (AerospikeException e) {
                LOGGER.error("ERROR :" + e);

            }
        } finally {
            item.close();
        }
        return recordsAsList;
    }

}
