package com.stratio.tests.utils.matchers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import cucumber.api.DataTable;

public class DBObjectsMatcher extends TypeSafeMatcher<ArrayList<DBObject>> {

	private final DataTable table;

	// Constructor
	public DBObjectsMatcher(DataTable table) {
		this.table = table;
	}

	@Factory
	public static DBObjectsMatcher containedInMongoDBResult(DataTable table) {
		return new DBObjectsMatcher(table);
	}

	@Override
	public void describeTo(Description description) {
		description
				.appendText("The result obtained is not equals from expected in DataTable");

	}

	@Override
	protected boolean matchesSafely(ArrayList<DBObject> item) {
		ArrayList<String[]> col_rel = coltoArrayList(table);

		for (int i = 1; i < table.raw().size(); i++) {
			// Obtenemos la fila correspondiente
			BasicDBObject doc = new BasicDBObject();
			List<String> row = table.raw().get(i);
			for (int x = 0; x < row.size(); x++) {
				String[] col_name_type = col_rel.get(x);
				Object data = castSTringTo(col_name_type[1], row.get(x));
				doc.put(col_name_type[0], data);
			}
			if (!isContained(item, doc)) {
				return false;
			}
		}
		return true;
	}

	private boolean isContained(ArrayList<DBObject> item, BasicDBObject doc) {
		boolean res = false;
		for (int i = 0; i < item.size() && !res; i++) {
			DBObject aux = item.get(i);
			aux.removeField("_id");
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

	private ArrayList<String[]> coltoArrayList(DataTable table) {
		ArrayList<String[]> res = new ArrayList<String[]>();
		// Primero se obiente la primera fila del datatable
		List<String> firstRow = table.raw().get(0);
		for (int i = 0; i < firstRow.size(); i++) {
			String[] col_type_array = firstRow.get(i).split("-");
			res.add(col_type_array);
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
