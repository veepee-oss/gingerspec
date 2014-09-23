package com.stratio.specs;

import com.mongodb.DBObject;

import static com.stratio.tests.utils.matchers.RecordSetMatcher.containedInRecordSet;
import static com.stratio.tests.utils.matchers.ExceptionMatcher.hasClassAndMessage;
import static com.stratio.tests.utils.matchers.ColumnDefinitionsMatcher.containsColumn;
import static com.stratio.tests.utils.matchers.ListLastElementExceptionMatcher.lastElementHasClassAndMessage;
import static com.stratio.tests.utils.matchers.DBObjectsMatcher.containedInMongoDBResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aerospike.client.query.RecordSet;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;

public class ThenGSpec extends BaseGSpec {

    public ThenGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    @Then("^an exception '(.*?)' thrown( with class '(.*?)'( and message like '(.*?)')?)?")
    public void assertExceptionNotThrown(String exception, String foo, String clazz, String bar, String exceptionMsg)
            throws ClassNotFoundException {
        commonspec.getLogger().info("Verifying thrown exceptions existance");

        List<Exception> exceptions = commonspec.getExceptions();
        if ("IS NOT".equals(exception)) {
            assertThat("Captured exception list is not empty", exceptions,
                    anyOf(hasSize(0), lastElementHasClassAndMessage("", "")));
        } else {
            assertThat("Captured exception list is empty", exceptions, hasSize(greaterThan((0))));
            Exception ex = exceptions.get(exceptions.size() - 1);
            if ((clazz != null) && (exceptionMsg != null)) {

                assertThat("Unexpected last exception class or message", ex, hasClassAndMessage(clazz, exceptionMsg));

            } else if (clazz != null) {
                assertThat("Unexpected last exception class", exceptions.get(exceptions.size() - 1).getClass()
                        .getSimpleName(), equalTo(clazz));
            }

            commonspec.getExceptions().clear();
        }
    }

    @Then("^a Casandra keyspace '(.*?)' exists$")
    public void assertKeyspaceOnCassandraExists(String keyspace) {
        commonspec.getLogger().info("Verifying if the keyspace {} exists", keyspace);
        assertThat("The keyspace " + keyspace + "exists on cassandra", commonspec.getCassandraClient().getKeyspaces(),
                hasItem(keyspace));
    }

    @Then("^a Casandra keyspace '(.*?)' contains a table '(.*?)'$")
    public void assertTableExistsOnCassandraKeyspace(String keyspace, String tableName) {
        commonspec.getLogger().info("Verifying if the table {} exists in the keyspace {}", tableName, keyspace);
        assertThat("The table " + tableName + "exists on cassandra", commonspec.getCassandraClient()
                .getTables(keyspace), hasItem(tableName));
    }

    @Then("^a Casandra keyspace '(.*?)' contains a table '(.*?)' with '(.*?)' rows$")
    public void assertRowNumberOfTableOnCassandraKeyspace(String keyspace, String tableName, String numberRows) {
        commonspec.getLogger().info("Verifying if the keyspace {} exists", keyspace);
        commonspec.getCassandraClient().useKeyspace(keyspace);
        assertThat("The table " + tableName + "exists on cassandra",
                commonspec.getCassandraClient().executeQuery("SELECT COUNT(*) FROM " + tableName + ";").all().get(0)
                        .getLong(0), equalTo(new Long(numberRows)));
    }

    @Then("^a Casandra keyspace '(.*?)' contains a table '(.*?)' with values:$")
    public void assertValuesOfTable(String keyspace, String tableName, DataTable data) throws InterruptedException {
        // Primero hacemos USE del Keyspace
        commonspec.getLogger().info("Verifying if the keyspace {} exists", keyspace);
        commonspec.getCassandraClient().useKeyspace(keyspace);
        // Obtenemos los tipos y los nombres de las columnas del datatable y los
        // devolvemos en un hashmap
        Map<String, String> dataTableColumns = extractColumnNamesAndTypes(data.raw().get(0));
        // Comprobamos que la tabla tenga las columnas
        String query = "SELECT * FROM " + tableName + " LIMIT 1;";
        ResultSet res = commonspec.getCassandraClient().executeQuery(query);
        equalsColumns(res.getColumnDefinitions(), dataTableColumns);
        // Obtenemos la cadena de la parte del select con las columnas
        // pertenecientes al dataTable
        List<String> selectQueries = giveQueriesList(data, tableName, columnNames(data.raw().get(0)));
        // Pasamos a comprobar los datos de cassandra con las distintas queries
        int index = 1;
        for (String execQuery : selectQueries) {
            res = commonspec.getCassandraClient().executeQuery(execQuery);
            List<Row> resAsList = res.all();
            assertThat("The query " + execQuery + " not return any result on Cassandra", resAsList.size(),
                    greaterThan(0));
            assertThat("The resultSet is not as expected", resAsList.get(0).toString().substring(3), equalTo(data.raw()
                    .get(index).toString()));
            index++;
        }
    }

    @SuppressWarnings("rawtypes")
    public void equalsColumns(ColumnDefinitions resCols, Map<String, String> dataTableColumns) {
        Iterator it = dataTableColumns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            assertThat("The table not contains the column.", resCols.toString(), containsColumn(e.getKey().toString()));
            DataType type = resCols.getType(e.getKey().toString());
            assertThat("The column type is not equals.", type.getName().toString(), equalTo(e.getValue().toString()));
        }
    }

    public void checkhatcolumnNameExists(String resultSetColumns, String columnName) {
        String[] aux = resultSetColumns.split("\\p{Punct}");
        ArrayList<String> test = new ArrayList<String>(Arrays.asList(aux));
        assertThat("The column " + columnName + " does not exists.", test, hasItem(columnName));
    }

    public List<String> giveQueriesList(DataTable data, String tableName, String colNames) {
        List<String> queryList = new ArrayList<String>();
        for (int i = 1; i < data.raw().size(); i++) {
            String query = "SELECT " + colNames + " FROM " + tableName;
            List<String> row = data.raw().get(i);
            query += conditionWhere(row, colNames.split(",")) + ";";
            queryList.add(query);
        }
        return queryList;
    }

    public String conditionWhere(List<String> values, String[] columnNames) {

        String condition = " WHERE ";
        Pattern numberPat = Pattern.compile("^\\d+(\\.*\\d*)?");
        Pattern booleanPat = Pattern.compile("true|false");
        for (int i = 0; i < values.size() - 1; i++) {
            if (numberPat.matcher(values.get(i)).matches() || booleanPat.matcher(values.get(i)).matches()) {
                condition += columnNames[i] + " = " + values.get(i) + " AND ";
            } else {
                condition += columnNames[i] + " = '" + values.get(i) + "' AND ";
            }
        }
        if (numberPat.matcher(values.get(values.size() - 1)).matches()
                || booleanPat.matcher(values.get(values.size() - 1)).matches()) {
            condition += columnNames[columnNames.length - 1] + " = " + values.get(values.size() - 1);
        } else {
            condition += columnNames[columnNames.length - 1] + " = '" + values.get(values.size() - 1) + "'";
        }
        return condition;
    }

    public String columnNames(List<String> firstRow) {
        String columnNamesForQuery = "";
        for (String s : firstRow) {
            String[] aux = s.split("-");
            columnNamesForQuery += aux[0] + ",";
        }
        columnNamesForQuery = columnNamesForQuery.substring(0, columnNamesForQuery.length() - 1);
        return columnNamesForQuery;
    }

    public Map<String, String> extractColumnNamesAndTypes(List<String> firstRow) {
        HashMap<String, String> columns = new HashMap<String, String>();
        for (String s : firstRow) {
            String[] aux = s.split("-");
            columns.put(aux[0], aux[1]);
        }
        return columns;
    }

    @Then("^the '(.*?)' index has a type '(.*?)' with content \\(key and value\\): '(.*?)'$")
    public void assertIndexHasContent(String indexName, String type, String data) {
        commonspec.getLogger().info("Verifying elasticseach content existance");

        List<String> responseList = new ArrayList<String>();
        List<String> cleanResponseList = new ArrayList<String>();
        for (String query : data.split(",")) {
            String response = commonspec.getElasticSearchClient().queryIndex(indexName, type, query);

            Pattern pat = Pattern.compile(".*?source.*?\\{(.*?)\\}.*?");
            Matcher m = pat.matcher(response);
            while (m.find()) {
                responseList.add(m.group(1).replaceAll("\"", ""));
            }
        }
        // drop dupe results
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(responseList);
        responseList.clear();
        responseList.addAll(hs);
        Collections.sort(responseList);
        // cleanup results, dropping timestamp
        for (String el : responseList) {
            cleanResponseList.add(el.replaceAll(",@timestamp.*", ""));
        }

        assertThat("Event not found at elastic search index", cleanResponseList, hasItem(data));
    }

    @Then("^checking if a Aerospike namespace '(.*?)' with table '(.*?)' and data exists:$")
    public void assertValuesOfTableAeroSpike(String nameSpace, String tableName, DataTable data) {
        commonspec.getLogger().info("Verifying if the nameSpace {} exists and tableName {} exists on Aerospike",
                nameSpace, tableName);
        RecordSet rs = commonspec.getAerospikeClient().readTable(nameSpace, tableName);
        assertThat("The table does not contains the data required.", rs, containedInRecordSet(data));
    }

    @Then("^a Mongo dataBase '(.*?)' contains a table '(.*?)' with values:")
    public void assertValuesOfTableMongo(String dataBase, String tableName, DataTable data) {
        commonspec.getLogger().info("Verifying if the dataBase {} exists and tableName {} exists on MongoDB", dataBase,
                tableName);
        commonspec.getMongoDBClient().connectToMongoDBDataBase(dataBase);
        ArrayList<DBObject> result = (ArrayList<DBObject>) commonspec.getMongoDBClient().readFromMongoDBCollection(
                tableName, data);
        assertThat("The table does not contains the data required.", result, containedInMongoDBResult(data));

    }

    @Then("^a Mongo dataBase '(.*?)' doesnt contains a table '(.*?)'$")
    public void aMongoDataBaseContainsaTable(String database, String tableName) {
        commonspec.getLogger().info("Verifying if the dataBase {} contains the table {}", database, tableName);
        commonspec.getMongoDBClient().connectToMongoDBDataBase(database);
        Set<String> collectionsNames = commonspec.getMongoDBClient().getMongoDBCollections();
        assertThat("The Mongo dataBase contains the table", collectionsNames, not(hasItem(tableName)));
    }

}