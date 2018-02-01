package com.privalia.qa.utils;


import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Generic operations on sql relational databases. Currently supports mysql/postgres
 */
public class SqlUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlUtils.class);

    private final String host;

    private final int port;

    private final String dataBaseType;

    private final String dataBaseName;

    private final Boolean security;

    private final String user;

    private final String password;

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
     * Generic constructor
     *
     * @param host
     * @param port
     * @param dataBaseType
     * @param dataBaseName
     * @param security
     * @param user
     * @param password
     */
    public SqlUtils(String host, int port, String dataBaseType, String dataBaseName, Boolean security, String user, String password) {
        this.host = host;
        this.port = port;
        this.dataBaseType = dataBaseType;
        this.dataBaseName = dataBaseName;
        this.security = security;
        this.user = user;
        this.password = password;
    }

    /**
     * Connect to the database
     */
    public void connect() throws ClassNotFoundException, SQLException {

        LOGGER.debug(String.format("Database type set to: %s", this.dataBaseType));

        switch (dataBaseType) {
            case "MYSQL":
                Class.forName("com.mysql.jdbc.Driver");
                break;

            case "POSTGRESQL":
                Class.forName("org.postgresql.Driver");
                break;

            default:
                Class.forName("org.postgresql.Driver");
                break;
        }


        Properties props = new Properties();
        String connectionString = "jdbc:" + this.dataBaseType.toLowerCase() + "://" + this.host + ":" + this.port + "/" + this.dataBaseName + "?user=" + this.user + "&password=" + this.password;
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
                .getConnection(connectionString);

    }

    public int executeQuery(String query) throws SQLException {

        Statement myStatement = null;
        int result = 0;

        try {
            myStatement = this.sqlConnection.createStatement();
            result = myStatement.executeUpdate(query);
        } finally {
            myStatement.close();
            return result;
        }

    }

    /**
     * Executes a SELECT type query on the DB connection. Instead of a resultset, this method
     * returns a List of Maps, where each Map represents a row with columnNames and columValues
     * @param query SELECT query to execute
     * @return List of Maps
     * @throws SQLException
     */
    public List<Map<String, Object>> executeSelectQuery(String query) {

        Statement myStatement = null;
        //postgres table
        List<String> sqlTable = new ArrayList<String>();
        List<String> sqlTableAux = new ArrayList<String>();
        Connection myConnection = this.sqlConnection;
        ResultSet rs = null;

        try {
            myStatement = myConnection.createStatement();
            rs = myStatement.executeQuery(query);
            List<Map<String, Object>> result = this.resultSetToList(rs);

            rs.close();
            myStatement.close();

            return result;

        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Verify if a table exists
     * @param tableName Table name
     * @return true if the table exists, false otherwise
     */
    private boolean verifyTable(String tableName){

        Statement myStatement = null;
        Connection myConnection = this.sqlConnection;
        boolean exists = false;

        String query = "SELECT * FROM pg_tables WHERE tablename = " + "\'" + tableName + "\'" + ";";
        try {
            myStatement = myConnection.createStatement();
            ResultSet rs = myStatement.executeQuery(query);
            //if there are no data row, table doesn't exists
            if (rs.next() == true) {
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
     * Convert the ResultSet to a List of Maps, where each Map represents a row with columnNames and columValues
     * @param rs
     * @return
     * @throws SQLException
     */
    private List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        while (rs.next()){
            Map<String, Object> row = new HashMap<String, Object>(columns);
            for(int i = 1; i <= columns; ++i){
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * Verify the state of the connection
     *
     * @return
     */
    public boolean connectionStatus() throws SQLException {

        return !this.sqlConnection.isClosed();
    }

    /**
     * Closes the connection to the database
     */
    public void disconnect() throws SQLException {

        this.sqlConnection.close();
    }


}
