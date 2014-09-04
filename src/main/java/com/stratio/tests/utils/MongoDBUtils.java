package com.stratio.tests.utils;

import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import cucumber.api.DataTable;
import cucumber.deps.com.thoughtworks.xstream.mapper.Mapper.Null;

public class MongoDBUtils {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MongoDBUtils.class);

	private final String host;
	private final int port;
	private MongoClient mongoClient;
	private DB dataBase;

	public MongoDBUtils() {
		this.host = System.getProperty("MONGODB_HOST", "127.0.0.1");
		this.port = 27017;
		LOGGER.debug("Initializing MongoDB.");
	}

	public void connectToMongoDB() throws UnknownHostException {
		mongoClient = new MongoClient(this.host, this.port);
	}

	public void connectToMongoDBDataBase(String db) {
		dataBase = mongoClient.getDB(db);
	}

	public boolean exitsMongoDbDataBase(String dataBaseName) {
		List<String> dataBaseList = mongoClient.getDatabaseNames();
		return dataBaseList.contains(dataBaseName);
	}

	public boolean exitsCollections(String col_name) {
		if (dataBase.collectionExists(col_name)) {
			return true;
		} else {
			return false;
		}
	}

	public Set<String> getMongoDBCollections() {
		return dataBase.getCollectionNames();
	}

	public DBCollection getMongoDBCollection(String collection_name) {
		return dataBase.getCollection(collection_name);
	}

	public void createMongoDBCollection(String dataBaseName, String colection_name,
			DataTable options) {
		connectToMongoDBDataBase(dataBaseName);
		BasicDBObject aux = new BasicDBObject();
		//Recorremos las options para castearlas y añadirlas a la collection
		List<List<String>> rows_op = options.raw();
		for(int i = 0; i < rows_op.size(); i++){
			List<String> row_op = rows_op.get(i);
			if(row_op.get(0).equals("size") ||row_op.get(0).equals("max")){
				int intproperty = Integer.parseInt(row_op.get(1));
				aux.append(row_op.get(0),intproperty);
			}else{
				Boolean bool_property = Boolean.parseBoolean(row_op.get(1));
				aux.append(row_op.get(0),bool_property);
			}
		}
		dataBase.createCollection(colection_name, aux);
	}
	
	public void dropMongoDBDataBase(String dataBaseName){
		mongoClient.dropDatabase(dataBaseName);
	}
	
	public void dropMongoDBCollection(String collection_name){
		getMongoDBCollection(collection_name).drop();
	}
	
	public void dropAllDataMongoDBCollection(String collection_name){
		DBCollection db = getMongoDBCollection(collection_name);
		List<DBObject> objects_list = db.getIndexInfo();
		for(int i = 0; i < objects_list.size(); i++){
			db.remove(objects_list.get(i));
		}
	}
	
	public void insertIntoMongoDBCollection(String dataBase, String collection, DataTable table){
		//Primero pasamos la fila del datatable a un hashmap de ColumnName-Type
		 ArrayList<String[]> col_rel = coltoArrayList(table);
		 DB db = mongoClient.getDB(dataBase);
		 //Vamos insertando fila a fila
		 for(int i = 1; i < table.raw().size(); i++){
			 //Obtenemos la fila correspondiente
			 BasicDBObject doc = new BasicDBObject();
			 List<String> row = table.raw().get(i);
			 for(int x = 0; x < row.size(); x++){
				 String[] col_name_type = col_rel.get(x);
				 Object data = castSTringTo(col_name_type[1], row.get(x));
				 doc.put(col_name_type[0], data);
			 }
			 db.getCollection(collection).insert(doc);
		 }
		
	}
	
	public void readFromMongoDBCollection(String dataBase, String collection,
			DataTable table) {
		ArrayList<String[]> col_rel = coltoArrayList(table);
		DB db = mongoClient.getDB(dataBase);
		DBCollection aux = db.getCollection(collection);
		for (int i = 1; i < table.raw().size(); i++) {
			// Obtenemos la fila correspondiente
			BasicDBObject doc = new BasicDBObject();
			List<String> row = table.raw().get(i);
			for (int x = 0; x < row.size(); x++) {
				String[] col_name_type = col_rel.get(x);
				Object data = castSTringTo(col_name_type[1], row.get(x));
				doc.put(col_name_type[0], data);
			}
			DBCursor cursor = aux.find(doc);
			try {
				while (cursor.hasNext()) {
					System.out.println(cursor.next());
				}
			} finally {
				cursor.close();
			}
		}

	}
	
	private ArrayList<String[]> coltoArrayList(DataTable table){
		ArrayList<String[]> res = new ArrayList<String[]>();
		//Primero se obiente la primera fila del datatable
		List<String> firstRow = table.raw().get(0);
		for(int i = 0; i < firstRow.size(); i++){
			String[] col_type_array = firstRow.get(i).split("-");
			res.add(col_type_array);
		}
		return res;
	}
	
	private Object castSTringTo(String dataType, String data){
		switch(dataType){
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
