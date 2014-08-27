package com.stratio.tests.utils;

import java.util.Hashtable;

public class QueryUtils {

	public String useQuery(String keyspace) {
		return "USE " + keyspace + ";";
	}

	public String createKeyspaceReplication(
			Hashtable<String, String> replication) {
		String result = "";
		if (!replication.isEmpty()) {
			// No uso String buffer porque como maximo seran siempre 2
			for (String s : replication.keySet()) {
				result += s + ": " + replication.get(s) + ", ";
			}
			result = result.substring(0, result.length() - 2);
		}
		return result;
	}

	public String createKeyspaceQuery(Boolean ifNotExists, String keyspaceName,
			String replication, String durable_writes) {
		String result = "CREATE KEYSPACE ";
		if (ifNotExists) {
			result = result + "IF NOT EXISTS ";
		}
		result = result + keyspaceName;
		if (replication != "" || durable_writes != "") {
			result += " WITH ";
			if (replication != "") {
				result += "REPLICATION = {" + replication + "}";
			}
			if (durable_writes != "") {
				if (replication != "") {
					result += " AND ";
				}
				result += "durable_writes = " + durable_writes;
			}
		}
		result = result + ";";
		return result;
	}

	public String dropKeyspaceQuery(Boolean ifExists, String keyspace) {
		String query = "DROP KEYSPACE ";
		if (ifExists)
			query += "IF EXISTS ";
		query += keyspace;
		return query += ";";
	}

	public String dropTableQuery(Boolean ifExists, String table) {
		String query = "DROP TABLE ";
		if (ifExists)
			query += "IF EXISTS ";
		query += table;
		return query += ";";
	}
}
