/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package com.privalia.qa.assertions;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.cucumber.datatable.DataTable;
import org.assertj.core.api.AbstractAssert;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DBObjectsAssert extends AbstractAssert<DBObjectsAssert, ArrayList<DBObject>> {


    public DBObjectsAssert(ArrayList<DBObject> actual) {
        super(actual, DBObjectsAssert.class);
    }

    public static DBObjectsAssert assertThat(ArrayList<DBObject> actual) {
        return new DBObjectsAssert(actual);
    }

    public DBObjectsAssert containedInMongoDBResult(DataTable table) {
        boolean resultado = matchesSafely(actual, table);
        if (resultado == false) {
            failWithMessage("The table does not contains the data required.");
        }
        return new DBObjectsAssert(actual);
    }

    protected boolean matchesSafely(ArrayList<DBObject> item, DataTable table) {
        List<String[]> colRel = coltoArrayList(table);

        for (int i = 1; i < table.height(); i++) {
            // Obtenemos la fila correspondiente
            BasicDBObject doc = new BasicDBObject();
            List<String> row = table.row(i);
            for (int x = 0; x < row.size(); x++) {
                String[] colNameType = colRel.get(x);
                Object data = castSTringTo(colNameType[1], row.get(x));
                doc.put(colNameType[0], data);
            }
            if (!isContained(item, doc)) {
                return false;
            }
        }
        return true;
    }

    private boolean isContained(List<DBObject> item, BasicDBObject doc) {
        boolean res = false;
        for (int i = 0; i < item.size() && !res; i++) {
            DBObject aux = item.get(i);
            aux.removeField("_id");
            aux.removeField("timestamp");

            if (aux.keySet().equals(doc.keySet())) {
                res = true;
            }
            // Obtenemos los columnNames
            List<String> cols = new ArrayList<String>(doc.keySet());
            for (int x = 0; x < cols.size() && res; x++) {
                if (!aux.get(cols.get(x)).equals(doc.get(cols.get(x)))) {
                    res = false;
                } else {
                    res = true;
                }
            }
        }
        return res;
    }


    private List<String[]> coltoArrayList(DataTable table) {
        List<String[]> res = new ArrayList<String[]>();
        // Primero se obiente la primera fila del datatable
        List<String> firstRow = table.row(0);
        for (int i = 0; i < firstRow.size(); i++) {
            String[] colTypeArray = firstRow.get(i).split("-");
            res.add(colTypeArray);
        }
        return res;
    }


    private Object castSTringTo(String dataType, String data) {
        switch (dataType) {
            case "String":
                return data;
            case "Integer":
                return Integer.parseInt(data);
            case "Double":
                return Double.parseDouble(dataType);
            case "Boolean":
                return Boolean.parseBoolean(dataType);
            case "Timestamp":
                return Timestamp.valueOf(dataType);
            default:
                return null;
        }
    }


}
