package com.stratio.tests.utils;

import java.util.Map;

/**
 * Build queries for cassandra.
 * 
 * @author Javier Delgado
 * @author Hugo Dominguez
 * 
 */
public class CassandraQueryUtils {
    
     /**
     * Use Keyspace Query.
     * 
     * @param keyspace
     * @return
     */
    public String useQuery(String keyspace) {
        return "USE " + keyspace + ";";
    }

    /**
     * Create the replication part of a query.
     * 
     * @param replication
     * @return
     */
    public String createKeyspaceReplication(Map<String, String> replication) {
        StringBuilder result = new StringBuilder();
        if (!replication.isEmpty()) {
            for (Map.Entry<String, String> entry : replication.entrySet()) {
                result.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
        }
        return result.toString().substring(0, result.length() - 2);
    }

    /**
     * Create Keyspace builder query.
     * 
     * @param ifNotExists
     * @param keyspaceName
     * @param replication
     * @param durableWrites
     * @return
     */
    public String createKeyspaceQuery(Boolean ifNotExists, String keyspaceName, String replication, String durableWrites) {
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

    /**
     * Drop keyspace builder query.
     * 
     * @param ifExists
     * @param keyspace
     * @return
     */
    public String dropKeyspaceQuery(Boolean ifExists, String keyspace) {
        String query = "DROP KEYSPACE ";
        if (ifExists) {
            query += "IF EXISTS ";
        }
        query = query + keyspace + ";";
        return query;
    }

    /**
     * Drop table builder query.
     * 
     * @param ifExists
     * @param table
     * @return
     */
    public String dropTableQuery(Boolean ifExists, String table) {
        String query = "DROP TABLE ";
        if (ifExists) {
            query += "IF EXISTS ";
        }
        query = query + table + ";";
        return query;
    }
}
