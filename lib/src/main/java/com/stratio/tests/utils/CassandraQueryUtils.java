package com.stratio.tests.utils;

import java.util.ArrayList;
import java.util.Map;

import cucumber.api.DataTable;

public class CassandraQueryUtils {

    public String createMapperGeoPoint(String keyspace, String table,String magic_column, String lat, String lon, String maxLevels){
        String query="CREATE CUSTOM INDEX index ON "+keyspace+"."+table+"("+magic_column+") USING 'com.stratio.cassandra.lucene.Index' WITH OPTIONS = { 'refresh_seconds' : '1', 'schema' : '{ fields : { geo_point : { type       : \"geo_point\", latitude   : "+lat+", longitude  : "+lon+", max_levels : "+maxLevels+"}}}'};";
        
   return query;     
    }
    
    
	public String searchGeoBbox(String table, String magic_colum, double min_latitude, double min_longitude, double max_latitude, double max_longitude, String filter_query, String field){
	    String query= "SELECT * FROM "+table+" WHERE "
	            +magic_colum+" = '{"+filter_query+" : "
	            + "{ type : \"geo_bbox\", "
	            + "field : \""+field+"\", "
	            + "min_latitude : "+min_latitude+", "
	            + "max_latitude : "+max_latitude+", "
	            + "min_longitude : "+min_longitude+", "
	            + "max_longitude : "+max_longitude+" }}';";

	    return query;
	    
	}
	
	   public String insertData(String table, Map<String, Object> fields){
	        String query= "INSERT INTO "+table+" (";
	          for(int i=0; i<fields.size()-1; i++){
	               query+=fields.keySet().toArray()[i]+", ";
	           }
	              query+=fields.keySet().toArray()[fields.size()-1]+") VALUES (";
          
	              for(int i=0; i<fields.size()-1; i++){

                  query+=""+fields.values().toArray()[i]+", ";
                  }
                  

                      query+=""+fields.values().toArray()[fields.size()-1]+");";

                  
	              
	        return query;
	        
	    }
	   public String createTable(String table, Map<String, String> colums, ArrayList<String> primaryKey){
         //  String query= "CREATE TABLE tweets (id INT PRIMARY KEY, user TEXT, body TEXT, time TIMESTAMP, latitude FLOAT, longitude FLOAT, lucene TEXT);";
           String query= "CREATE TABLE "+table+" (";

	       for(int i=0; i<colums.size(); i++){
	           query+=colums.keySet().toArray()[i]+" "+colums.values().toArray()[i]+", ";
	       }
	       query = query + "PRIMARY KEY(";
	       if(primaryKey.size()==1){
	           query +=primaryKey.get(0)+"));";

	       }else{
	       for(int e=0; e<primaryKey.size()-1; e++){
	       
	       query +=primaryKey.get(e)+", ";
	       }
           query +=primaryKey.get(primaryKey.size()-1)+"));";
	       }
	       System.out.println(query);
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
}
