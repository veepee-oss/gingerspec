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
package com.stratio.qa.specs;

import com.datastax.driver.core.*;
import com.stratio.qa.exceptions.DBException;
import com.stratio.qa.utils.ThreadProperty;
import cucumber.api.DataTable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;


// cassandra table creation and data insertion test
// with equalsColumns comparison
public class CassandraToolsIT extends BaseGSpec {
    public static final int VALUE_SUBSTRING = 3;
    GivenGSpec commonspecG;
    //for table creation
    List<List<String>> dataCreation = Arrays.asList(Arrays.asList("col1", "col2"),
            Arrays.asList("text", "int"),
            Arrays.asList("PK", "PK")
    );

    //for data insertion
    List<List<String>> dataInsertion = Arrays.asList(
            Arrays.asList("col1", "col2"),
            Arrays.asList("\'a\'", "4")
    );

    // to compare
    List<List<String>> dataComparison = Arrays.asList(Arrays.asList("col1-varchar", "col2-int"),
            Arrays.asList("a", "4")
    );

    List<List<String>> dataComparison2 = Arrays.asList(Arrays.asList("col5-varchar", "col3-int"),
            Arrays.asList("a", "4")
    );

    //DataTable convertion
    DataTable dataTableCreation = DataTable.create(dataCreation);
    DataTable dataTable1Insertion = DataTable.create(dataInsertion);
    DataTable dataTableComparison = DataTable.create(dataComparison);
    DataTable dataTableComparison2 = DataTable.create(dataComparison2);

    String tableName = "testcstring";
    String keySpace = "stratio_decision";

    public CassandraToolsIT() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        this.commonspec = new CommonG();
        commonspecG = new GivenGSpec(this.commonspec);
    }

    @BeforeClass
    public void prepareCassandra() {
        commonspec.getCassandraClient().connect();
        commonspec.getCassandraClient().createKeyspace(this.keySpace);
        commonspecG.createTableWithData(this.tableName, this.keySpace, this.dataTableCreation);
        commonspecG.insertData(this.keySpace, this.tableName, dataTable1Insertion);
    }

    @Test
    public void test_assertTableExistance() {
        assertThat(commonspec.getCassandraClient().existsTable(this.keySpace,this.tableName,false)).isEqualTo(true);
    }

        @Test
    public void test_assertValuesOfTable_success() {

        // USE of Keyspace
        commonspec.getLogger().debug("Verifying if the keyspace {} exists", this.keySpace);
        commonspec.getCassandraClient().useKeyspace(this.keySpace);
        // Obtain the types and column names of the datatable
        // to return in a hashmap,
        // dataTableColumns uses to simulate the behavior
        //of data reception as  "col1-varchar", "col2-int"
        Map<String, String> dataTableColumns = extractColumnNamesAndTypes(this.dataTableComparison.raw().get(0));
        // check the table to have columns
        String query = "SELECT * FROM " + this.tableName + " LIMIT 1;";
        ResultSet res = commonspec.getCassandraClient().executeQuery(query);
        Iterator it = dataTableColumns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            assertThat(res.getColumnDefinitions().toString()).as("The table not contains the column.").contains(e.getKey().toString());
            DataType type = res.getColumnDefinitions().getType(e.getKey().toString());
            assertThat(type.getName().toString()).as("The column type is not equals.").isEqualTo(e.getValue().toString());
        }
    }

    @Test
    public void test_assertValuesOfTable_fail() {
        // USE of Keyspace
        commonspec.getLogger().debug("Verifying if the keyspace {} exists", this.keySpace);
        commonspec.getCassandraClient().useKeyspace(this.keySpace);
        // Obtain the types and column names of the datatable
        // to return in a hashmap,
        // dataTableColumns uses to simulate the behavior
        //of data reception as  "col1-varchar", "col2-int"
        Map<String, String> dataTableColumns = extractColumnNamesAndTypes(this.dataTableComparison2.raw().get(0));
        // check the table to have columns
        String query = "SELECT * FROM " + this.tableName + " LIMIT 1;";
        ResultSet res = commonspec.getCassandraClient().executeQuery(query);
        Iterator it = dataTableColumns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            assertThat(res.getColumnDefinitions().toString()).as("The table not contains the column.").doesNotContain(e.getKey().toString());
        }
    }

    @Test
    public void test_assertValuesOfTable_completeTable() throws InterruptedException, DBException {
        //  USE of Keyspace
        commonspec.getLogger().debug("Verifying if the keyspace {} exists", this.keySpace);
        commonspec.getCassandraClient().useKeyspace(this.keySpace);
        // Obtain the types and column names of the datatable
        // to return in a hashmap,
        Map<String, String> dataTableColumns = extractColumnNamesAndTypes(this.dataTableComparison.raw().get(0));
        // check if the table has columns
        String query = "SELECT * FROM " + this.tableName + " LIMIT 1;";
        ResultSet res = commonspec.getCassandraClient().executeQuery(query);
        equalsColumns(res.getColumnDefinitions(), dataTableColumns);
        //receiving the string from the select with the columns
        // that belong to the dataTable
        List<String> selectQueries = giveQueriesList(this.dataTableComparison, tableName, columnNames(this.dataTableComparison.raw().get(0)));
        //Check the data  of cassandra with different queries
        int index = 1;
        for (String execQuery : selectQueries) {
            res = commonspec.getCassandraClient().executeQuery(execQuery);
            List<Row> resAsList = res.all();
            assertThat(resAsList.size()).as("The query " + execQuery + " not return any result on Cassandra").isGreaterThan(0);
            assertThat(resAsList.get(0).toString()
                    .substring(VALUE_SUBSTRING)).as("The resultSet is not as expected").isEqualTo(this.dataTableComparison.raw().get(index).toString());
            index++;
        }
    }

    @Test
    public void testCassandraMetaData() throws DBException {
        Metadata metaData = commonspec.getCassandraClient().getMetadata();
        assert metaData.getClusterName().equals("Stratio cluster");
    }

    @AfterClass
    public void freeCassandra() {
        commonspec.getCassandraClient().dropKeyspace(this.keySpace);
        //connect(String "Cassandra", String ${CASSANDRA_HOST});
        try {
            commonspec.getCassandraClient().disconnect();
        } catch (DBException e) {
            e.printStackTrace();
        }
    }

    // "-" extraction
    private Map<String, String> extractColumnNamesAndTypes(List<String> firstRow) {
        HashMap<String, String> columns = new HashMap<String, String>();
        for (String s : firstRow) {
            String[] aux = s.split("-");
            columns.put(aux[0], aux[1]);
        }
        return columns;
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

}
