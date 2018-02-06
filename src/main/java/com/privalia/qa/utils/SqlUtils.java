package com.privalia.qa.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;


/**
 * Generic operations on sql relational databases. Currently supports mysql/postgres
 */
public class SqlUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlUtils.class);

    private String host;

    private int port;

    private String dataBaseType;

    private String dataBaseName;

    private Boolean security;

    private String user;

    private String password;

    private Connection sqlConnection;

    /**
     * Generic constructor.
     */
    public SqlUtils() {
        this.dataBaseType = System.getProperty("SQLDB_TYPE", "MYSQL");
        this.host = System.getProperty("SQLDB_HOST", "172.17.0.1");
        this.port = Integer.parseInt(System.getProperty("SQLDB_PORT", "3306"));
        this.dataBaseName = System.getProperty("SQLDB_NAME", "sys");
        this.security = Boolean.parseBoolean(System.getProperty("SQLDB_SECURITY", "false"));
        this.user = System.getProperty("SQLDB_USER", "root");
        this.password = System.getProperty("SQLDB_PASSWORD", "mysql");
    }

    /**
     * Connect to the database
     */
    public void connect(String host, int port, String dataBaseType, String dataBaseName, Boolean security, String user, String password) throws ClassNotFoundException, SQLException {

        LOGGER.debug(String.format("Database type set to: %s", this.dataBaseType));
        this.dataBaseType = dataBaseType;
        this.dataBaseName = dataBaseName;

        switch (dataBaseType.toUpperCase()) {
            case "MYSQL":
                Class.forName("com.mysql.cj.jdbc.Driver");
                break;

            case "POSTGRESQL":
                Class.forName("org.postgresql.Driver");
                break;

            default:
                Class.forName("org.postgresql.Driver");
                break;
        }


        Properties props = new Properties();
        String connectionString = "jdbc:" + dataBaseType.toLowerCase() + "://" + host + ":" + port + "/" + dataBaseName + "?user=" + user + "&password=" + password;
        LOGGER.debug(String.format("Starting connection using %s", connectionString));

        if (this.security) {

            // TODO: 1/02/18 Create case when using SSL, adding correct props
//            props.setProperty("user", user);
//            props.setProperty("password", password);
//            props.setProperty("ssl", "true");
//            props.setProperty("sslmode", "verify-full");
//            props.setProperty("sslcert", "src/test/resources/credentials/postgres.crt");
//            props.setProperty("sslkey", "src/test/resources/credentials/postgresql.pk8");
//            props.setProperty("sslrootcert", "src/test/resources/credentials/stratio-ca.crt");

            this.sqlConnection = DriverManager
                    .getConnection(connectionString, props);

            return;
        }

        this.sqlConnection = DriverManager
                .getConnection(connectionString + "&useSSL=false");

    }

    public int executeQuery(String query) throws SQLException {

        int result = 0;

        LOGGER.debug(String.format("Executing query %s", query));
        try(Statement myStatement = this.sqlConnection.createStatement()) {
            result = myStatement.executeUpdate(query);
            return result;
        }
    }

    /**
     * Executes a SELECT type query on the DB connection. Instead of a resultset, this method
     * returns a List of lists (List<List<String>>). This method will return in the first list the
     * columns name, and the remaining lists are the rows (if the query returned any). So, this method
     * will always return at least 1 List (size 1).
     * This way of representing a resulset is very similar to the structure of a datatable in
     * cucumber, making the comparison easier
     * @param query SELECT query to execute
     * @return List of Maps
     * @throws SQLException
     */
    public List<List<String>> executeSelectQuery(String query) throws SQLException {

        List<List<String>> sqlTable = new ArrayList<>();
        ResultSet rs = null;

        try (Statement myStatement = this.sqlConnection.createStatement()) {
            LOGGER.debug(String.format("Executing query %s", query));
            rs = myStatement.executeQuery(query);

            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            int count = resultSetMetaData.getColumnCount();

            /*Store the column names*/
            List<String> columnHeader = new LinkedList<>();
            for (int i = 1; i <= count; i++) {
                columnHeader.add(resultSetMetaData.getColumnName(i));
            }
            sqlTable.add(columnHeader);

            /*Store the values*/
            while (rs.next()) {
                List<String> sqlTableAux = new LinkedList<>();
                for (int i = 1; i <= count; i++) {
                    sqlTableAux.add(rs.getObject(i).toString());
                }
                sqlTable.add(sqlTableAux);
            }

            return sqlTable;
        }

    }

    /**
     * Verify if a table exists
     * @param tableName Table name
     * @return true if the table exists, false otherwise
     */
    public boolean verifyTable(String tableName) throws SQLException {

        Statement myStatement = null;
        Connection myConnection = this.sqlConnection;
        boolean exists = false;

        String query;

        if (this.dataBaseType.toLowerCase().matches("mysql")) {
            query = "SELECT * FROM information_schema.tables WHERE table_schema = '" + this.sqlConnection.getCatalog() + "' AND table_name = '" + tableName + "' LIMIT 1;";
        } else {
            query = "SELECT * FROM pg_tables WHERE tablename = " + "\'" + tableName + "\'" + ";";
        }

        LOGGER.debug(String.format("Verifying if table %s exists. Executing %s", tableName, query));
        try  {
            myStatement = myConnection.createStatement();
            ResultSet rs = myStatement.executeQuery(query);
            //if there are no data row, table doesn't exists
            if (rs.next()) {
                exists = true;
            }
            rs.close();
            myStatement.close();
            return exists;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Verify the state of the connection
     *
     * @return
     */
    public boolean connectionStatus() {

        LOGGER.debug("Checking DB connection status");
        boolean status = false;
        try {
            status = !this.sqlConnection.isClosed();
            return status;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Closes the connection to the database
     */
    public void disconnect() throws SQLException {

        LOGGER.debug(String.format("Closing connection to DB %s in %s:%s", this.dataBaseName, this.host, this.port));
        this.sqlConnection.close();
    }


}
