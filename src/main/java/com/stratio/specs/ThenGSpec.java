package com.stratio.specs;

import static com.stratio.assertions.Assertions.assertThat;
import static com.stratio.tests.utils.matchers.ColumnDefinitionsMatcher.containsColumn;
import static com.stratio.tests.utils.matchers.DBObjectsMatcher.containedInMongoDBResult;
import static com.stratio.tests.utils.matchers.ExceptionMatcher.hasClassAndMessage;
import static com.stratio.tests.utils.matchers.ListLastElementExceptionMatcher.lastElementHasClassAndMessage;
import static com.stratio.tests.utils.matchers.RecordSetMatcher.containedInRecordSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebElement;

import com.aerospike.client.query.RecordSet;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.mongodb.DBObject;
import com.stratio.cucumber.converter.ArrayListConverter;

import cucumber.api.DataTable;
import cucumber.api.Transform;
import cucumber.api.java.en.Then;

/**
 * @author Hugo Dominguez
 * @author Javier Delgado
 * 
 *         Then generic Specs that could be used in tests projects.
 */

public class ThenGSpec extends BaseGSpec {

    public static final int VALUE_SUBSTRING = 3;

    /**
     * Class constructor.
     * 
     * @param spec
     */
    public ThenGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Checks if an exception has been thrown.
     * 
     * @param exception
     *            : "IS NOT" | "IS"
     * @param foo
     * @param clazz
     * @param bar
     * @param exceptionMsg
     */
    @Then("^an exception '(.*?)' thrown( with class '(.*?)'( and message like '(.*?)')?)?")
    public void assertExceptionNotThrown(String exception, String foo, String clazz, String bar, String exceptionMsg)
            throws ClassNotFoundException {
        commonspec.getLogger().info("Verifying thrown exceptions existance");

        List<Exception> exceptions = commonspec.getExceptions();
        if ("IS NOT".equals(exception)) {
            org.hamcrest.MatcherAssert.assertThat("Captured exception list is not empty", exceptions,
                    anyOf(hasSize(0), lastElementHasClassAndMessage("", "")));
        } else {
            org.hamcrest.MatcherAssert.assertThat("Captured exception list is empty", exceptions,
                    hasSize(greaterThan((0))));
            Exception ex = exceptions.get(exceptions.size() - 1);
            if ((clazz != null) && (exceptionMsg != null)) {

                org.hamcrest.MatcherAssert.assertThat("Unexpected last exception class or message", ex,
                        hasClassAndMessage(clazz, exceptionMsg));

            } else if (clazz != null) {
                org.hamcrest.MatcherAssert.assertThat("Unexpected last exception class",
                        exceptions.get(exceptions.size() - 1).getClass().getSimpleName(), equalTo(clazz));
            }

            commonspec.getExceptions().clear();
        }
    }

    /**
     * Checks if a keyspaces exists in Cassandra.
     * 
     * @param keyspace
     */
    @Then("^a Casandra keyspace '(.*?)' exists$")
    public void assertKeyspaceOnCassandraExists(String keyspace) {
        commonspec.getLogger().info("Verifying if the keyspace {} exists", keyspace);
        org.hamcrest.MatcherAssert.assertThat("The keyspace " + keyspace + "exists on cassandra", commonspec
                .getCassandraClient().getKeyspaces(), hasItem(keyspace));
    }

    /**
     * Checks if a cassandra keyspace contains a table.
     * 
     * @param keyspace
     * @param tableName
     */
    @Then("^a Casandra keyspace '(.*?)' contains a table '(.*?)'$")
    public void assertTableExistsOnCassandraKeyspace(String keyspace, String tableName) {
        commonspec.getLogger().info("Verifying if the table {} exists in the keyspace {}", tableName, keyspace);
        org.hamcrest.MatcherAssert.assertThat("The table " + tableName + "exists on cassandra", commonspec
                .getCassandraClient().getTables(keyspace), hasItem(tableName));
    }

    /**
     * Checks the number of rows in a cassandra table.
     * 
     * @param keyspace
     * @param tableName
     * @param numberRows
     */
    @Then("^a Casandra keyspace '(.*?)' contains a table '(.*?)' with '(.*?)' rows$")
    public void assertRowNumberOfTableOnCassandraKeyspace(String keyspace, String tableName, String numberRows) {
        Long numberRowsLong = Long.parseLong(numberRows);
        commonspec.getLogger().info("Verifying if the keyspace {} exists", keyspace);
        commonspec.getCassandraClient().useKeyspace(keyspace);
        org.hamcrest.MatcherAssert.assertThat("The table " + tableName + "exists on cassandra", commonspec
                .getCassandraClient().executeQuery("SELECT COUNT(*) FROM " + tableName + ";").all().get(0).getLong(0),
                equalTo(numberRowsLong));
    }

    /**
     * Checks if a cassandra table contains the values of a DataTable.
     * 
     * @param keyspace
     * @param tableName
     * @param data
     * @throws InterruptedException
     */
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
            org.hamcrest.MatcherAssert.assertThat("The query " + execQuery + " not return any result on Cassandra",
                    resAsList.size(), greaterThan(0));
            org.hamcrest.MatcherAssert.assertThat("The resultSet is not as expected", resAsList.get(0).toString()
                    .substring(VALUE_SUBSTRING), equalTo(data.raw().get(index).toString()));
            index++;
        }
    }

    @SuppressWarnings("rawtypes")
    private void equalsColumns(ColumnDefinitions resCols, Map<String, String> dataTableColumns) {
        Iterator it = dataTableColumns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            org.hamcrest.MatcherAssert.assertThat("The table not contains the column.", resCols.toString(),
                    containsColumn(e.getKey().toString()));
            DataType type = resCols.getType(e.getKey().toString());
            org.hamcrest.MatcherAssert.assertThat("The column type is not equals.", type.getName().toString(),
                    equalTo(e.getValue().toString()));
        }
    }

    private List<String> giveQueriesList(DataTable data, String tableName, String colNames) {
        List<String> queryList = new ArrayList<String>();
        for (int i = 1; i < data.raw().size(); i++) {
            String query = "SELECT " + colNames + " FROM " + tableName;
            List<String> row = data.raw().get(i);
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
     * Checks if the index has a specific content.
     * 
     * @param indexName
     * @param type
     * @param data
     */
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

        org.hamcrest.MatcherAssert.assertThat("Event not found at elastic search index", cleanResponseList,
                hasItem(data));
    }

    /**
     * Checks the values of a Aerospike table.
     * 
     * @param nameSpace
     * @param tableName
     * @param data
     */
    @Then("^checking if a Aerospike namespace '(.*?)' with table '(.*?)' and data exists:$")
    public void assertValuesOfTableAeroSpike(String nameSpace, String tableName, DataTable data) {
        commonspec.getLogger().info("Verifying if the nameSpace {} exists and tableName {} exists on Aerospike",
                nameSpace, tableName);
        RecordSet rs = commonspec.getAerospikeClient().readTable(nameSpace, tableName);
        org.hamcrest.MatcherAssert.assertThat("The table does not contains the data required.", rs,
                containedInRecordSet(data));
    }

    /**
     * Checks the values of a MongoDB table.
     * 
     * @param dataBase
     * @param tableName
     * @param data
     */
    @Then("^a Mongo dataBase '(.*?)' contains a table '(.*?)' with values:")
    public void assertValuesOfTableMongo(String dataBase, String tableName, DataTable data) {
        commonspec.getLogger().info("Verifying if the dataBase {} exists and tableName {} exists on MongoDB", dataBase,
                tableName);
        commonspec.getMongoDBClient().connectToMongoDBDataBase(dataBase);
        ArrayList<DBObject> result = (ArrayList<DBObject>) commonspec.getMongoDBClient().readFromMongoDBCollection(
                tableName, data);
        org.hamcrest.MatcherAssert.assertThat("The table does not contains the data required.", result,
                containedInMongoDBResult(data));

    }

    /**
     * Checks if a MongoDB database contains a table.
     * 
     * @param database
     * @param tableName
     */
    @Then("^a Mongo dataBase '(.*?)' doesnt contains a table '(.*?)'$")
    public void aMongoDataBaseContainsaTable(String database, String tableName) {
        commonspec.getLogger().info("Verifying if the dataBase {} contains the table {}", database, tableName);
        commonspec.getMongoDBClient().connectToMongoDBDataBase(database);
        Set<String> collectionsNames = commonspec.getMongoDBClient().getMongoDBCollections();
        org.hamcrest.MatcherAssert.assertThat("The Mongo dataBase contains the table", collectionsNames,
                not(hasItem(tableName)));
    }

    /**
     * Checks if a text exists in the source of an already loaded URL.
     * 
     * @param text
     */
    @Then("^a text '(.*?)' exists$")
    public void assertTextInSource(String text) {
        commonspec.getLogger().info("Verifying if our current page contains the text {}", text);
        assertThat(commonspec.getDriver()).as("Expected text not found at page").contains(text);
    }

    /**
     * Checks if the first element found has an expecific text.
     * 
     * @param element
     * @param texts
     */
    @Then("^an element '([^:]*?):([^:]*?)' has '(.*?)' as content$")
    public void assertTextInElement(String method, String element,
            @Transform(ArrayListConverter.class) List<String> texts) {
        commonspec.getLogger().info("Verifying text content of elements {}", texts);

        List<WebElement> wel = commonspec.locateElement(method, element);

        assertThat(wel).as("No element with with " + element + " attribute found").isNotEmpty();
        String[] expectedTexts = texts.toArray(new String[texts.size()]);
        assertThat(wel.get(0)).as("Element doesnt contains expected text").contains(expectedTexts);
    }

    /**
     * Queries a web each {@code poll} minutes, for a maximum of {@code totalTime} minutes, until at least an element
     * with attribute {@code attrib} with a value {@code element} exists.
     * 
     * @param totalTime
     * @param poll
     * @param attrib
     * @param element
     */
    @Then("^waiting during '(.*?)' minutes and polling every '(.*?)', an element '([^:]*?):([^:]*?)' exists$")
    public void pollForElement(Integer totalTime, Integer poll, String method, String element)
            throws InterruptedException {
        commonspec.getLogger().info("Waiting for element to be available: {} minutes", totalTime);
        List<WebElement> wel = commonspec.locateElement(method, element);
        int i = totalTime;

        while ((i >= 0) && (wel.size() == 0)) {
            i = i - poll;
            Thread.sleep(poll * 1000 * 60);
            commonspec.getLogger().info("Waited {}", totalTime - i);
            wel = commonspec.locateElement(method, element);
        }

        assertThat(wel).as("No element with with " + element + " attribute found").isNotEmpty();
    }

    /**
     * Verifies that the first webelement found identified as {@code element} has {@code text} as text
     * 
     * @param element
     * @param text
     */
    @Then("^an element '([^:]*?):([^:]*?)' does has a text '(.*?)'$")
    public void textOnElementPresent(String method, String element, String text) {
        commonspec.getLogger().info("Verifying text unexistance");

        List<WebElement> wel = commonspec.locateElement(method, element);
        assertThat(wel).as("No element with with " + element + " attribute found").isNotEmpty();
        assertThat(wel.get(0)).contains(text);
    }

    /**
     * Checks if the first element found has an expecific text.
     * 
     * @param target
     * @param texts
     */
    @Then("^an element '([^:]*?):([^:]*?)' exists")
    public void assertElementExists(String method, String element) {
        commonspec.getLogger().info("Verifying elements { existance}", element);

        List<WebElement> wel = commonspec.locateElement(method, element);

        assertThat(wel).as("Element " + element + " not found").isNotEmpty();
    }

    /**
     * Takes an snapshot of the current page
     * 
     * @param target
     * @param texts
     * @throws Exception
     */
    @Then("^I take a snapshot$")
    public void takeSnapshot() throws Exception {

        commonspec.captureEvidence(commonspec.getDriver(), "screenCapture");
    }
}