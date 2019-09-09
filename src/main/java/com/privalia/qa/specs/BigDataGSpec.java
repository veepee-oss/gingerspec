package com.privalia.qa.specs;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;
import com.ning.http.client.Response;
import com.privalia.qa.exceptions.DBException;
import com.privalia.qa.utils.ThreadProperty;

import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Future;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import io.cucumber.datatable.DataTable;
import org.elasticsearch.common.xcontent.XContentBuilder;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.privalia.qa.assertions.DBObjectsAssert;
import org.apache.zookeeper.KeeperException;
import java.util.regex.Pattern;

import static com.privalia.qa.assertions.Assertions.assertThat;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


/**
 * Steps definition for big data functionality
 */
public class BigDataGSpec extends BaseGSpec {

    public static final Integer ES_DEFAULT_NATIVE_PORT = 9300;

    public static final String ES_DEFAULT_CLUSTER_NAME = "elasticsearch";

    public static final int VALUE_SUBSTRING = 3;

    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public BigDataGSpec(CommonG spec) {
        this.commonspec = spec;

    }

    /**
     * Create a basic Index.
     *
     * @param index_name index name
     * @param table      the table where index will be created.
     * @param column     the column where index will be saved
     * @param keyspace   keyspace used
     * @throws Exception exception
     */
    @Given("^I create a Cassandra index named '(.+?)' in table '(.+?)' using magic_column '(.+?)' using keyspace '(.+?)'$")
    public void createBasicMapping(String index_name, String table, String column, String keyspace) throws Exception {
        String query = "CREATE INDEX " + index_name + " ON " + table + " (" + column + ");";
        commonspec.getCassandraClient().executeQuery(query);
    }

    /**
     * Create a Cassandra Keyspace.
     *
     * @param keyspace cassandra keyspace
     */
    @Given("^I create a Cassandra keyspace named '(.+)'$")
    public void createCassandraKeyspace(String keyspace) {
        commonspec.getCassandraClient().createKeyspace(keyspace);
    }

    /**
     * Connect to cluster.
     *
     * @param clusterType               DB type (Cassandra|Mongo|Elasticsearch)
     * @param url                       url where is started Cassandra cluster
     * @throws DBException              DBException
     * @throws UnknownHostException     UnknownHostException
     */
    @Given("^I connect to '(Cassandra|Mongo|Elasticsearch)' cluster at '(.+)'$")
    public void connect(String clusterType, String url) throws DBException, UnknownHostException {
        switch (clusterType) {
            case "Cassandra":
                commonspec.getCassandraClient().buildCluster();
                commonspec.getCassandraClient().connect();
                break;
            case "Mongo":
                commonspec.getMongoDBClient().connect();
                break;
            case "Elasticsearch":
                LinkedHashMap<String, Object> settings_map = new LinkedHashMap<String, Object>();
                settings_map.put("cluster.name", System.getProperty("ES_CLUSTER", ES_DEFAULT_CLUSTER_NAME));
                commonspec.getElasticSearchClient().setSettings(settings_map);
                commonspec.getElasticSearchClient().connect();
                break;
            default:
                throw new DBException("Unknown cluster type");
        }
    }

    /**
     * Connect to ElasticSearch using custom parameters
     *
     * @param host ES host
     * @param foo regex needed to match method
     * @param nativePort ES port
     * @param bar regex needed to match method
     * @param clusterName ES clustername
     * @throws DBException exception
     * @throws UnknownHostException exception
     * @throws NumberFormatException exception
     */
    @Given("^I connect to Elasticsearch cluster at host '(.+?)'( using native port '(.+?)')?( using cluster name '(.+?)')?$")
    public void connectToElasticSearch(String host, String foo, String nativePort, String bar, String clusterName) throws DBException, UnknownHostException, NumberFormatException {
        LinkedHashMap<String, Object> settings_map = new LinkedHashMap<String, Object>();
        if (clusterName != null) {
            settings_map.put("cluster.name", clusterName);
        } else {
            settings_map.put("cluster.name", ES_DEFAULT_CLUSTER_NAME);
        }
        commonspec.getElasticSearchClient().setSettings(settings_map);
        if (nativePort != null) {
            commonspec.getElasticSearchClient().setNativePort(Integer.valueOf(nativePort));
        } else {
            commonspec.getElasticSearchClient().setNativePort(ES_DEFAULT_NATIVE_PORT);
        }
        commonspec.getElasticSearchClient().setHost(host);
        commonspec.getElasticSearchClient().connect();
    }

    /**
     * Create table
     *
     * @param table Cassandra table
     * @param datatable datatable used for parsing elements
     * @param keyspace Cassandra keyspace
     */
    @Given("^I create a Cassandra table named '(.+?)' using keyspace '(.+?)' with:$")
    public void createTableWithData(String table, String keyspace, DataTable datatable) {
        try {
            commonspec.getCassandraClient().useKeyspace(keyspace);
            int attrLength = datatable.width();
            Map<String, String> columns = new HashMap<String, String>();
            ArrayList<String> pk = new ArrayList<String>();

            for (int i = 0; i < attrLength; i++) {
                columns.put(datatable.row(0).get(i),
                        datatable.row(1).get(i));
                if ((datatable.height() == 3) && datatable.row(2).get(i).equalsIgnoreCase("PK")) {
                    pk.add(datatable.row(0).get(i));
                }
            }
            if (pk.isEmpty()) {
                throw new Exception("A PK is needed");
            }
            commonspec.getCassandraClient().createTableWithData(table, columns, pk);
        } catch (Exception e) {
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
    }

    /**
     * Insert Data
     *
     * @param table Cassandra table
     * @param datatable datatable used for parsing elements
     * @param keyspace Cassandra keyspace
     */
    @Given("^I insert in keyspace '(.+?)' and table '(.+?)' with:$")
    public void insertData(String keyspace, String table, DataTable datatable) {
        try {
            commonspec.getCassandraClient().useKeyspace(keyspace);
            int attrLength = datatable.width();
            Map<String, Object> fields = new HashMap<String, Object>();
            for (int e = 1; e < datatable.height(); e++) {
                for (int i = 0; i < attrLength; i++) {
                    fields.put(datatable.row(0).get(i), datatable.row(e).get(i));

                }
                commonspec.getCassandraClient().insertData(keyspace + "." + table, fields);

            }
        } catch (Exception e) {
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
    }

    /**
     * Save clustername of elasticsearch in an environment varible for future use.
     *
     * @param host   elasticsearch connection
     * @param envVar thread variable where to store the value
     * @throws IllegalAccessException exception
     * @throws IllegalArgumentException exception
     * @throws SecurityException exception
     * @throws NoSuchFieldException exception
     * @throws ClassNotFoundException exception
     * @throws InstantiationException exception
     * @throws InvocationTargetException exception
     * @throws NoSuchMethodException exception
     */
    @Given("^I obtain elasticsearch cluster name in '(.+?)' and save it in variable '(.+?)'?$")
    public void saveElasticCluster(String host, String envVar) throws Exception {

        String port = "9300";
        String[] address = host.split(":");

        if (address.length == 2) {
            host = address[0];
            port = address[1];
        }

        RestSpec restSpec = new RestSpec(this.commonspec);
        restSpec.setupApp(null, host + ":" + port);

        Future<Response> response;

        response = commonspec.generateRequest("GET", false, null, null, "/", "", "json", "");
        commonspec.setResponse("GET", response.get());

        String json;
        String parsedElement;
        json = commonspec.getResponse().getResponse();
        parsedElement = "$..cluster_name";

        String json2 = "[" + json + "]";
        String value = commonspec.getJSONPathString(json2, parsedElement, "0");

        if (value == null) {
            throw new Exception("No cluster name is found");
        } else {
            ThreadProperty.set(envVar, value);
        }
    }

    /**
     * Drop all the ElasticSearch indexes.
     */
    @Given("^I drop every existing elasticsearch index$")
    public void dropElasticsearchIndexes() {
        commonspec.getElasticSearchClient().dropAllIndexes();
    }

    /**
     * Drop an specific index of ElasticSearch.
     *
     * @param index ES index
     */
    @Given("^I drop an elasticsearch index named '(.+?)'$")
    public void dropElasticsearchIndex(String index) {
        commonspec.getElasticSearchClient().dropSingleIndex(index);
    }

    /**
     * Drop a Cassandra Keyspace.
     *
     * @param keyspace Cassandra keyspace
     */
    @Given("^I drop a Cassandra keyspace '(.+)'$")
    public void dropCassandraKeyspace(String keyspace) {
        commonspec.getCassandraClient().dropKeyspace(keyspace);
    }

    /**
     * Create a MongoDB dataBase.
     *
     * @param databaseName Mongo database
     */
    @Given("^I create a MongoDB dataBase '(.+?)'$")
    public void createMongoDBDataBase(String databaseName) {
        commonspec.getMongoDBClient().connectToMongoDBDataBase(databaseName);

    }

    /**
     * Drop MongoDB Database.
     *
     * @param databaseName mongo database
     */
    @Given("^I drop a MongoDB database '(.+?)'$")
    public void dropMongoDBDataBase(String databaseName) {
        commonspec.getMongoDBClient().dropMongoDBDataBase(databaseName);
    }

    /**
     * Insert data in a MongoDB table.
     *
     * @param dataBase Mongo database
     * @param tabName Mongo table
     * @param table Datatable used for insert elements
     */
    @Given("^I insert into a MongoDB database '(.+?)' and table '(.+?)' this values:$")
    public void insertOnMongoTable(String dataBase, String tabName, DataTable table) {
        commonspec.getMongoDBClient().connectToMongoDBDataBase(dataBase);
        commonspec.getMongoDBClient().insertIntoMongoDBCollection(tabName, table);
    }

    /**
     * Truncate table in MongoDB.
     *
     * @param database Mongo database
     * @param table Mongo table
     */
    @Given("^I drop every document at a MongoDB database '(.+?)' and table '(.+?)'")
    public void truncateTableInMongo(String database, String table) {
        commonspec.getMongoDBClient().connectToMongoDBDataBase(database);
        commonspec.getMongoDBClient().dropAllDataMongoDBCollection(table);
    }

    /**
     * Insert document in a MongoDB table.
     *
     * @param dataBase      Mongo database
     * @param collection    Mongo collection
     * @param document      document used for schema
     * @throws Exception    Exception
     */
    @Given("^I insert into MongoDB database '(.+?)' and collection '(.+?)' the document from schema '(.+?)'$")
    public void insertOnMongoTable(String dataBase, String collection, String document) throws Exception {
        String retrievedDoc = commonspec.retrieveData(document, "json");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(dataBase);
        commonspec.getMongoDBClient().insertDocIntoMongoDBCollection(collection, retrievedDoc);
    }

    /**
     * Connect to zookeeper.
     *
     * @param zookeeperHosts as host:port (comma separated)
     * @throws InterruptedException exception
     */
    @Given("^I connect to Zookeeper at '(.+)'$")
    public void connectToZk(String zookeeperHosts) throws InterruptedException {
        commonspec.getZookeeperSecClient().setZookeeperSecConnection(zookeeperHosts, 3000);
        commonspec.getZookeeperSecClient().connectZk();
    }

    /**
     * Disconnect from zookeeper.
     *
     * @throws InterruptedException InterruptedException
     */
    @Given("^I disconnect from Zookeeper$")
    public void disconnectFromZk() throws InterruptedException {
        commonspec.getZookeeperSecClient().disconnect();
    }

    /**
     * Execute a query with schema over a cluster
     *
     * @param fields        columns on which the query is executed. Example: "latitude,longitude" or "*" or "count(*)"
     * @param schema        the file of configuration (.conf) with the options of mappin. If schema is the word "empty", method will not add a where clause.
     * @param type          type of the changes in schema (string or json)
     * @param table         table for create the index
     * @param magic_column  magic column where index will be saved. If you don't need index, you can add the word "empty"
     * @param keyspace      keyspace used
     * @param modifications all data in "where" clause. Where schema is "empty", query has not a where clause. So it is necessary to provide an empty table. Example:  ||.
     */
    @When("^I execute a query over fields '(.+?)' with schema '(.+?)' of type '(json|string)' with magic_column '(.+?)' from table: '(.+?)' using keyspace: '(.+?)' with:$")
    public void sendQueryOfType(String fields, String schema, String type, String magic_column, String table, String keyspace, DataTable modifications) {
        try {
            commonspec.setResultsType("cassandra");
            commonspec.getCassandraClient().useKeyspace(keyspace);
            commonspec.getLogger().debug("Starting a query of type " + commonspec.getResultsType());

            String query = "";

            if (schema.equals("empty") && magic_column.equals("empty")) {

                query = "SELECT " + fields + " FROM " + table + ";";

            } else if (!schema.equals("empty") && magic_column.equals("empty")) {
                String retrievedData = commonspec.retrieveData(schema, type);
                String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
                query = "SELECT " + fields + " FROM " + table + " WHERE " + modifiedData + ";";


            } else {
                String retrievedData = commonspec.retrieveData(schema, type);
                String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
                query = "SELECT " + fields + " FROM " + table + " WHERE " + magic_column + " = '" + modifiedData + "';";

            }
            commonspec.getLogger().debug("query: {}", query);
            commonspec.setCassandraResults(commonspec.getCassandraClient().executeQuery(query));
        } catch (Exception e) {
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }


    }

    /**
     * Execute a query on (mongo) database
     *
     * @param query         path to query
     * @param type          type of data in query (string or json)
     * @param database      MongoDB database
     * @param collection    collection in database
     * @param modifications modifications to perform in query
     * @throws Exception    Exception
     */
    @When("^I execute a query '(.+?)' of type '(json|string)' in mongo '(.+?)' database using collection '(.+?)' with:$")
    public void sendQueryOfType(String query, String type, String database, String collection, DataTable modifications) throws Exception {
        try {
            commonspec.setResultsType("mongo");
            String retrievedData = commonspec.retrieveData(query, type);
            String modifiedData = commonspec.modifyData(retrievedData, type, modifications);
            commonspec.getMongoDBClient().connectToMongoDBDataBase(database);
            DBCollection dbCollection = commonspec.getMongoDBClient().getMongoDBCollection(collection);
            DBObject dbObject = (DBObject) JSON.parse(modifiedData);
            DBCursor cursor = dbCollection.find(dbObject);
            commonspec.setMongoResults(cursor);
        } catch (Exception e) {
            commonspec.getExceptions().add(e);
        }
    }

    /**
     * Execute query with filter over elasticsearch
     *
     * @param indexName     index name
     * @param mappingName   mapping name
     * @param columnName    column Name
     * @param filterType    it could be equals, gt, gte, lt and lte.
     * @param value         value of the column to be filtered.
     */
    @When("^I execute an elasticsearch query over index '(.*?)' and mapping '(.*?)' and column '(.*?)' with value '(.*?)' to '(.*?)'$")
    public void elasticSearchQueryWithFilter(String indexName, String mappingName, String
            columnName, String filterType, String value) {
        try {
            commonspec.setResultsType("elasticsearch");
            commonspec.setElasticsearchResults(
                    commonspec.getElasticSearchClient()
                            .searchSimpleFilterElasticsearchQuery(indexName, mappingName, columnName,
                                    value, filterType)
            );
        } catch (Exception e) {
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
    }

    /**
     * Create a Cassandra index.
     *
     * @param index_name    index name
     * @param schema        the file of configuration (.conf) with the options of mappin
     * @param type          type of the changes in schema (string or json)
     * @param table         table for create the index
     * @param magic_column  magic column where index will be saved
     * @param keyspace      keyspace used
     * @param modifications data introduced for query fields defined on schema
     * @throws Exception    Exception
     */
    @When("^I create a Cassandra index named '(.+?)' with schema '(.+?)' of type '(json|string)' in table '(.+?)' using magic_column '(.+?)' using keyspace '(.+?)' with:$")
    public void createCustomMapping(String index_name, String schema, String type, String table, String magic_column, String keyspace, DataTable modifications) throws Exception {
        String retrievedData = commonspec.retrieveData(schema, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
        String query = "CREATE CUSTOM INDEX " + index_name + " ON " + keyspace + "." + table + "(" + magic_column + ") "
                + "USING 'com.dummy.cassandra.lucene.Index' WITH OPTIONS = " + modifiedData;
        commonspec.getLogger().debug("Will execute a cassandra query: {}", query);
        commonspec.getCassandraClient().executeQuery(query);
    }

    /**
     * Drop table
     *
     * @param table     Table name
     * @param keyspace  Keyspace
     */
    @When("^I drop a Cassandra table named '(.+?)' using keyspace '(.+?)'$")
    public void dropTableWithData(String table, String keyspace) {
        try {
            commonspec.getCassandraClient().useKeyspace(keyspace);
            commonspec.getCassandraClient().dropTable(table);
        } catch (Exception e) {
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
    }

    /**
     * Truncate table
     *
     * @param table         table name
     * @param keyspace      keyspace
     */
    @When("^I truncate a Cassandra table named '(.+?)' using keyspace '(.+?)'$")
    public void truncateTable(String table, String keyspace) {
        try {
            commonspec.getCassandraClient().useKeyspace(keyspace);
            commonspec.getCassandraClient().truncateTable(table);
        } catch (Exception e) {
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
    }

    /**
     * Delete zPath, it should be empty
     *
     * @param zNode         Path at zookeeper
     * @throws Exception    Exception
     */
    @When("^I remove the zNode '(.+?)'$")
    public void removeZNode(String zNode) throws Exception {
        commonspec.getZookeeperSecClient().delete(zNode);
    }

    /**
     * Create zPath and domcument
     *
     * @param path          path at zookeeper
     * @param foo           a dummy match group
     * @param content       if it has content it should be defined
     * @param ephemeral     if it's created as ephemeral or not
     * @throws Exception    Exception
     */
    @When("^I create the zNode '(.+?)'( with content '(.+?)')? which (IS|IS NOT) ephemeral$")
    public void createZNode(String path, String foo, String content, boolean ephemeral) throws Exception {
        if (content != null) {
            commonspec.getZookeeperSecClient().zCreate(path, content, ephemeral);
        } else {
            commonspec.getZookeeperSecClient().zCreate(path, ephemeral);
        }
    }

    /**
     * Create an elasticsearch index.
     *
     * @param index         index
     * @param removeIndex   removeIndex
     */
    @When("^I create an elasticsearch index named '(.+?)'( removing existing index if exist)?$")
    public void createElasticsearchIndex(String index, String removeIndex) {
        if (removeIndex != null && commonspec.getElasticSearchClient().indexExists(index)) {
            commonspec.getElasticSearchClient().dropSingleIndex(index);
        }
        commonspec.getElasticSearchClient().createSingleIndex(index);
    }

    /**
     * Index a document within a mapping type.
     *
     * @param indexName         indexName
     * @param mappingName       mappingName
     * @param key               key
     * @param value             value
     * @throws Exception        Exception
     */
    @When("^I index a document in the index named '(.+?)' using the mapping named '(.+?)' with key '(.+?)' and value '(.+?)'$")
    public void indexElasticsearchDocument(String indexName, String mappingName, String key, String value) throws Exception {
        ArrayList<XContentBuilder> mappingsource = new ArrayList<XContentBuilder>();
        XContentBuilder builder = jsonBuilder().startObject().field(key, value).endObject();
        mappingsource.add(builder);
        commonspec.getElasticSearchClient().createMapping(indexName, mappingName, mappingsource);
    }

    /**
     * Checks if a keyspaces exists in Cassandra.
     *
     * @param keyspace  keyspace
     */
    @Then("^a Cassandra keyspace '(.+?)' exists$")
    public void assertKeyspaceOnCassandraExists(String keyspace) {
        assertThat(commonspec.getCassandraClient().getKeyspaces()).as("The keyspace " + keyspace + "exists on cassandra").contains(keyspace);
    }

    /**
     * Checks if a cassandra keyspace contains a table.
     *
     * @param keyspace      keyspace
     * @param tableName     tableName
     */
    @Then("^a Cassandra keyspace '(.+?)' contains a table '(.+?)'$")
    public void assertTableExistsOnCassandraKeyspace(String keyspace, String tableName) {
        assertThat(commonspec.getCassandraClient().getTables(keyspace)).as("The table " + tableName + "exists on cassandra").contains(tableName);
    }

    /**
     * Checks the number of rows in a cassandra table.
     *
     * @param keyspace      keyspace
     * @param tableName     tableName
     * @param numberRows    numberRows
     */
    @Then("^a Cassandra keyspace '(.+?)' contains a table '(.+?)' with '(.+?)' rows$")
    public void assertRowNumberOfTableOnCassandraKeyspace(String keyspace, String tableName, String numberRows) {
        Long numberRowsLong = Long.parseLong(numberRows);
        commonspec.getCassandraClient().useKeyspace(keyspace);
        assertThat(commonspec.getCassandraClient().executeQuery("SELECT COUNT(*) FROM " + tableName + ";").all().get(0).getLong(0)).as("The table " + tableName + "exists on cassandra").
                isEqualTo(numberRowsLong);
    }

    /**
     * Checks if a cassandra table contains the values of a DataTable.
     *
     * @param keyspace                  keyspace
     * @param tableName                 tableName
     * @param data                      data
     * @throws InterruptedException     InterruptedException
     */
    @Then("^a Cassandra keyspace '(.+?)' contains a table '(.+?)' with values:$")
    public void assertValuesOfTable(String keyspace, String tableName, DataTable data) throws InterruptedException {
        //  USE of Keyspace
        commonspec.getCassandraClient().useKeyspace(keyspace);
        // Obtain the types and column names of the datatable
        // to return in a hashmap,
        Map<String, String> dataTableColumns = extractColumnNamesAndTypes(data.row(0));
        // check if the table has columns
        String query = "SELECT * FROM " + tableName + " LIMIT 1;";
        ResultSet res = commonspec.getCassandraClient().executeQuery(query);
        equalsColumns(res.getColumnDefinitions(), dataTableColumns);
        //receiving the string from the select with the columns
        // that belong to the dataTable
        List<String> selectQueries = giveQueriesList(data, tableName, columnNames(data.row(0)));
        //Check the data  of cassandra with different queries
        int index = 1;
        for (String execQuery : selectQueries) {
            res = commonspec.getCassandraClient().executeQuery(execQuery);
            List<Row> resAsList = res.all();
            assertThat(resAsList.size()).as("The query " + execQuery + " not return any result on Cassandra").isGreaterThan(0);
            assertThat(resAsList.get(0).toString()
                    .substring(VALUE_SUBSTRING)).as("The resultSet is not as expected").isEqualTo(data.row(index).toString());
            index++;
        }
    }

    @SuppressWarnings("rawtypes")
    private void equalsColumns(ColumnDefinitions resCols, Map<String, String> dataTableColumns) {
        Iterator it = dataTableColumns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            assertThat(resCols.toString()).as("The table not contains the column.").contains(e.getKey().toString());
            DataType type = resCols.getType(e.getKey().toString());
            assertThat(type.getName().toString()).as("The column type is not equals.").isEqualTo(e.getValue().toString());
        }
    }

    private List<String> giveQueriesList(DataTable data, String tableName, String colNames) {
        List<String> queryList = new ArrayList<String>();
        for (int i = 1; i < data.height(); i++) {
            String query = "SELECT " + colNames + " FROM " + tableName;
            List<String> row = data.row(i);
            query += conditionWhere(row, colNames.split(",")) + ";";
            queryList.add(query);
        }
        return queryList;
    }

    private String conditionWhere(List<String> values, String[] columnNames) {
        StringBuilder condition = new StringBuilder();
        condition.append(" WHERE ");
        Pattern numberPat = Pattern.compile("^\\d+(\\.*\\d*)?");
        Pattern booleanPat = Pattern.compile("true|false");
        for (int i = 0; i < values.size() - 1; i++) {
            condition.append(columnNames[i]).append(" =");
            if (numberPat.matcher(values.get(i)).matches() || booleanPat.matcher(values.get(i)).matches()) {
                condition.append(" ").append(values.get(i)).append(" AND ");
            } else {
                condition.append(" '").append(values.get(i)).append("' AND ");
            }
        }
        condition.append(columnNames[columnNames.length - 1]).append(" =");
        if (numberPat.matcher(values.get(values.size() - 1)).matches()
                || booleanPat.matcher(values.get(values.size() - 1)).matches()) {
            condition.append(" ").append(values.get(values.size() - 1));
        } else {
            condition.append(" '").append(values.get(values.size() - 1)).append("'");
        }
        return condition.toString();
    }

    private String columnNames(List<String> firstRow) {
        StringBuilder columnNamesForQuery = new StringBuilder();
        for (String s : firstRow) {
            String[] aux = s.split("-");
            columnNamesForQuery.append(aux[0]).append(",");
        }
        return columnNamesForQuery.toString().substring(0, columnNamesForQuery.length() - 1);
    }

    private Map<String, String> extractColumnNamesAndTypes(List<String> firstRow) {
        HashMap<String, String> columns = new HashMap<String, String>();
        for (String s : firstRow) {
            String[] aux = s.split("-");
            columns.put(aux[0], aux[1]);
        }
        return columns;
    }

    /**
     * Checks the values of a MongoDB table.
     *
     * @param dataBase      dataBase
     * @param tableName     tableName
     * @param data          data
     */
    @Then("^a Mongo dataBase '(.+?)' contains a table '(.+?)' with values:")
    public void assertValuesOfTableMongo(String dataBase, String tableName, DataTable data) {
        commonspec.getMongoDBClient().connectToMongoDBDataBase(dataBase);
        ArrayList<DBObject> result = (ArrayList<DBObject>) commonspec.getMongoDBClient().readFromMongoDBCollection(
                tableName, data);
        DBObjectsAssert.assertThat(result).containedInMongoDBResult(data);

    }

    /**
     * Checks if a MongoDB database contains a table.
     *
     * @param database      database
     * @param tableName     tableName
     */
    @Then("^a Mongo dataBase '(.+?)' doesnt contains a table '(.+?)'$")
    public void aMongoDataBaseContainsaTable(String database, String tableName) {
        commonspec.getMongoDBClient().connectToMongoDBDataBase(database);
        Set<String> collectionsNames = commonspec.getMongoDBClient().getMongoDBCollections();
        assertThat(collectionsNames).as("The Mongo dataBase contains the table").doesNotContain(tableName);
    }

    /**
     * Read zPath
     *
     * @param zNode         path at zookeeper
     * @param document      expected content of znode
     * @param foo           Required parameter for optional string
     * @throws Exception    Exception
     */
    @Then("^the zNode '(.+?)' exists( and contains '(.+?)')?$")
    public void checkZnodeExists(String zNode, String foo, String document)  throws Exception {
        if (document == null) {
            String breakpoint = commonspec.getZookeeperSecClient().zRead(zNode);
            assert breakpoint.equals("") : "The zNode does not exist";
        } else {
            assert commonspec.getZookeeperSecClient().zRead(zNode).contains(document) : "The zNode does not exist or the content does not match";
        }
    }


    @Then("^the zNode '(.+?)' does not exist")
    public void checkZnodeNotExist(String zNode) throws Exception {
        assert !commonspec.getZookeeperSecClient().exists(zNode) : "The zNode exists";
    }

    /**
     * Check that the ElasticSearch index exists.
     *
     * @param indexName indexName
     */
    @Then("^An elasticsearch index named '(.+?)' exists")
    public void elasticSearchIndexExist(String indexName) {
        assert (commonspec.getElasticSearchClient().indexExists(indexName)) : "There is no index with that name";
    }

    /**
     * Check that the ElasticSearch index does not exist.
     *
     * @param indexName     indexName
     */
    @Then("^An elasticsearch index named '(.+?)' does not exist")
    public void elasticSearchIndexDoesNotExist(String indexName) {
        assert !commonspec.getElasticSearchClient().indexExists(indexName) : "There is an index with that name";
    }

    /**
     * Check that an elasticsearch index contains a specific document
     *
     * @param indexName     indexName
     * @param columnName    columnName
     * @param columnValue   columnValue
     * @param mappingName   mappingName
     * @throws Exception    Exception
     */
    @Then("^The Elasticsearch index named '(.+?)' and mapping '(.+?)' contains a column named '(.+?)' with the value '(.+?)'$")
    public void elasticSearchIndexContainsDocument(String indexName, String mappingName, String columnName, String columnValue) throws Exception {
        assertThat((commonspec.getElasticSearchClient().searchSimpleFilterElasticsearchQuery(
                indexName,
                mappingName,
                columnName,
                columnValue,
                "equals"
        ).size()) > 0).isTrue().withFailMessage("The index does not contain that document");
    }

}
