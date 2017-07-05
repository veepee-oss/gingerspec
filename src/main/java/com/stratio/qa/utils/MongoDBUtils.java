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

import com.mongodb.*;
import com.mongodb.util.JSON;
import com.stratio.qa.exceptions.DBException;
import cucumber.api.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Generic operations over MongoDB Driver.
 */
public class MongoDBUtils {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MongoDBUtils.class);

    private final String host;

    private final int port;

    private MongoClient mongoClient;

    private DB dataBase;

    /**
     * Generic constructor.
     */
    public MongoDBUtils() {
        this.host = System.getProperty("MONGO_HOST", "127.0.0.1");
        this.port = Integer.parseInt(System.getProperty("MONGO_PORT", "27017"));
    }

    /**
     * Connect to MongoDB Host.
     *
     * @throws DBException
     */
    public void connect() throws DBException {
        try {
            LOGGER.debug("Initializing MongoDB client");
            mongoClient = new MongoClient(this.host, this.port);
        } catch (UnknownHostException e) {
            throw new DBException(e.toString());

        }
    }

    /**
     * Disconnect of MongoDB host.
     */
    public void disconnect() {
        mongoClient.close();
    }

    /**
     * Connect to DataBase of MongoDB(If it not exists, it will be created).
     *
     * @param db
     */
    public void connectToMongoDBDataBase(String db) {
        dataBase = mongoClient.getDB(db);
    }

    /**
     * Checks if a database exists in MongoDB.
     *
     * @param dataBaseName
     * @return boolean
     */
    public boolean exitsMongoDbDataBase(String dataBaseName) {
        List<String> dataBaseList = mongoClient.getDatabaseNames();
        return dataBaseList.contains(dataBaseName);
    }

    /**
     * Checks if a collection exists in a MongoDB dataBase.
     *
     * @param colName
     * @return boolean
     */
    public boolean exitsCollections(String colName) {
        return dataBase.collectionExists(colName);
    }

    /**
     * Get a list of collections of a database.
     *
     * @return {@code Set<String>}
     */
    public Set<String> getMongoDBCollections() {
        return dataBase.getCollectionNames();
    }

    /**
     * Get a MongoDB collection.
     *
     * @param collectionName
     * @return DBCollection
     */
    public DBCollection getMongoDBCollection(String collectionName) {
        return dataBase.getCollection(collectionName);
    }

    /**
     * Create a MongoDB collection.
     *
     * @param colectionName
     * @param options
     */
    public void createMongoDBCollection(String colectionName, DataTable options) {
        BasicDBObject aux = new BasicDBObject();
        // Recorremos las options para castearlas y añadirlas a la collection
        List<List<String>> rowsOp = options.raw();
        for (int i = 0; i < rowsOp.size(); i++) {
            List<String> rowOp = rowsOp.get(i);
            if (rowOp.get(0).equals("size") || rowOp.get(0).equals("max")) {
                int intproperty = Integer.parseInt(rowOp.get(1));
                aux.append(rowOp.get(0), intproperty);
            } else {
                Boolean boolProperty = Boolean.parseBoolean(rowOp.get(1));
                aux.append(rowOp.get(0), boolProperty);
            }
        }
        dataBase.createCollection(colectionName, aux);
    }

    /**
     * Create a MongoDB collection without options.
     *
     * @param colectionName
     */
    public void createMongoDBCollection(String colectionName) {
        dataBase.createCollection(colectionName, null);
    }

    /**
     * Drop a MongoDB DataBase.
     *
     * @param dataBaseName
     */
    public void dropMongoDBDataBase(String dataBaseName) {
        mongoClient.dropDatabase(dataBaseName);
    }

    /**
     * Drop a MongoDBCollection.
     *
     * @param collectionName
     */
    public void dropMongoDBCollection(String collectionName) {
        getMongoDBCollection(collectionName).drop();
    }

    /**
     * Drop all the data associated to a MongoDB Collection.
     *
     * @param collectionName
     */
    public void dropAllDataMongoDBCollection(String collectionName) {
        DBCollection db = getMongoDBCollection(collectionName);
        DBCursor objectsList = db.find();
        try {
            while (objectsList.hasNext()) {
                db.remove(objectsList.next());
            }
        } finally {
            objectsList.close();
        }
    }

    /**
     * Insert data in a MongoDB Collection.
     *
     * @param collection
     * @param table
     */
    public void insertIntoMongoDBCollection(String collection, DataTable table) {
        // Primero pasamos la fila del datatable a un hashmap de ColumnName-Type
        List<String[]> colRel = coltoArrayList(table);
        // Vamos insertando fila a fila
        for (int i = 1; i < table.raw().size(); i++) {
            // Obtenemos la fila correspondiente
            BasicDBObject doc = new BasicDBObject();
            List<String> row = table.raw().get(i);
            for (int x = 0; x < row.size(); x++) {
                String[] colNameType = colRel.get(x);
                Object data = castSTringTo(colNameType[1], row.get(x));
                doc.put(colNameType[0], data);
            }
            this.dataBase.getCollection(collection).insert(doc);
        }
    }

    /**
     * Insert document in a MongoDB Collection.
     *
     * @param collection
     * @param document
     */
    public void insertDocIntoMongoDBCollection(String collection, String document) {

        DBObject dbObject = (DBObject) JSON.parse(document);
        this.dataBase.getCollection(collection).insert(dbObject);

    }

    /**
     * Read data from a MongoDB collection.
     *
     * @param collection
     * @param table
     * @return {@code List<DBObjects>}
     */
    public List<DBObject> readFromMongoDBCollection(String collection,
                                                    DataTable table) {
        List<DBObject> res = new ArrayList<DBObject>();
        List<String[]> colRel = coltoArrayList(table);
        DBCollection aux = this.dataBase.getCollection(collection);
        for (int i = 1; i < table.raw().size(); i++) {
            // Obtenemos la fila correspondiente
            BasicDBObject doc = new BasicDBObject();
            List<String> row = table.raw().get(i);
            for (int x = 0; x < row.size(); x++) {
                String[] colNameType = colRel.get(x);
                Object data = castSTringTo(colNameType[1], row.get(x));
                doc.put(colNameType[0], data);
            }
            DBCursor cursor = aux.find(doc);
            try {
                while (cursor.hasNext()) {
                    res.add(cursor.next());
                }
            } finally {
                cursor.close();
            }
        }
        return res;

    }

    private List<String[]> coltoArrayList(DataTable table) {
        List<String[]> res = new ArrayList<String[]>();
        // Primero se obiente la primera fila del datatable
        List<String> firstRow = table.raw().get(0);
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
