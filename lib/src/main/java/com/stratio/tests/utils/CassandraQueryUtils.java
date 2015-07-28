package com.stratio.tests.utils;

import java.util.Map;

public class CassandraQueryUtils {

	public String useQuery(String keyspace) {
		return "USE " + keyspace + ";";
	}

	public String createKeyspaceReplication(Map<String, String> replication) {
		StringBuilder result = new StringBuilder();
		if (!replication.isEmpty()) {
			for (Map.Entry<String, String> entry : replication.entrySet()) {
				result.append(entry.getKey()).append(": ")
						.append(entry.getValue()).append(", ");
			}
		}
		return result.toString().substring(0, result.length() - 2);
	}

	public String createKeyspaceQuery(Boolean ifNotExists, String keyspaceName,
			String replication, String durableWrites) {
		String result = "CREATE KEYSPACE ";
		if (ifNotExists) {
			result = result + "IF NOT EXISTS ";
		}
		result = result + keyspaceName;
		if (!"".equals(replication) || !"".equals(durableWrites)) {
			result += " WITH ";
			if (!"".equals(replication)) {
				result += "REPLICATION = {" + replication + "}";
			}
			if (!"".equals(durableWrites)) {
				if (!"".equals(replication)) {
					result += " AND ";
				}
				result += "durable_writes = " + durableWrites;
			}
		}
		result = result + ";";
		return result;
	}

	public String dropKeyspaceQuery(Boolean ifExists, String keyspace) {
		String query = "DROP KEYSPACE ";
		if (ifExists) {
			query += "IF EXISTS ";
		}
		query = query + keyspace + ";";
		return query;
	}

	public String dropTableQuery(Boolean ifExists, String table) {
		String query = "DROP TABLE ";
		if (ifExists) {
			query += "IF EXISTS ";
		}
		query = query + table + ";";
		return query;
	}
}
