package com.stratio.tests.utils.matchers;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.TypeSafeMatcher;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.query.RecordSet;

import cucumber.api.DataTable;

public class RecordSetMatcher extends TypeSafeMatcher<RecordSet> {
	private final DataTable table;

	// Constructor
	public RecordSetMatcher(DataTable table) {
		this.table = table;
	}

	@Factory
	public static RecordSetMatcher containedInRecordSet(DataTable table) {
		return new RecordSetMatcher(table);
	}

	@Override
	public void describeTo(Description description) {
		description
				.appendText("The data of the table is not contained in the RecordSet");
	}

	@Override
	protected boolean matchesSafely(RecordSet item) {
		List<List<String>> table_as_list = table.raw();
		List<String> columnNames = table_as_list.get(0);
		List<List<String>> records_as_table = recordToList(columnNames, item);
		//Primero comprobamos las longitudes(han de ser las mismas)
		if(records_as_table.size() != table_as_list.size()){
			return false;
		}
		//Pasamos a comprobar el contenido del recordset por filas
		for(int i = 1; i < table_as_list.size(); i++){
			if(records_as_table.contains(table_as_list.get(i))){
				records_as_table.remove(table_as_list.get(i));
			}else{
				return false;
			}
		}
		return true;
	}

	private List<List<String>> recordToList(List<String> columnNames, RecordSet item){
		List<List<String>> records_as_list= new ArrayList<List<String>>();
		records_as_list.add(columnNames);
		try {
			try {
				while (item.next()) {
					Record record = item.getRecord();
					List<String> record_as_list = new ArrayList<String>();
					for (int i = 0; i < columnNames.size(); i++) {
						Object aux = record.getValue(columnNames.get(i));
						if(aux != null){
							record_as_list.add(aux.toString());
						}else{
							record_as_list.add("ERROR");
						}

					}
					records_as_list.add(record_as_list);
				}
			} catch (AerospikeException e) {
				System.out.println(e.getMessage());

			}
		} finally {
			item.close();
		}
		return records_as_list;
	}

}
