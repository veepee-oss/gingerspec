/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.qa.utils;

import java.util.ArrayList;
import java.util.Map;

public class CassandraQueryUtils {


    public String insertData(String table, Map<String, Object> fields) {
        String query = "INSERT INTO " + table + " (";
        for (int i = 0; i < fields.size() - 1; i++) {
            query += fields.keySet().toArray()[i] + ", ";
        }
        query += fields.keySet().toArray()[fields.size() - 1] + ") VALUES (";

        for (int i = 0; i < fields.size() - 1; i++) {

            query += "" + fields.values().toArray()[i] + ", ";
        }


        query += "" + fields.values().toArray()[fields.size() - 1] + ");";


        return query;

    }

    public String createTable(String table, Map<String, String> colums, ArrayList<String> primaryKey) {
        String query = "CREATE TABLE " + table + " (";

        for (int i = 0; i < colums.size(); i++) {
            query += colums.keySet().toArray()[i] + " " + colums.values().toArray()[i] + ", ";
        }
        query = query + "PRIMARY KEY(";
        if (primaryKey.size() == 1) {
            query += primaryKey.get(0) + "));";

        } else {
            for (int e = 0; e < primaryKey.size() - 1; e++) {

                query += primaryKey.get(e) + ", ";
            }
            query += primaryKey.get(primaryKey.size() - 1) + "));";
        }
        return query;

    }

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

    public String truncateTableQuery(Boolean ifExists, String table) {
        String query = "TRUNCATE TABLE ";
        if (ifExists) {
            query += "IF EXISTS ";
        }
        query = query + table + ";";
        return query;
    }

}
