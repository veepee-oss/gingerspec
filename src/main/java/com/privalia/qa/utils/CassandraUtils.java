/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
*/

package com.privalia.qa.utils;

import com.datastax.driver.core.*;
import com.privalia.qa.exceptions.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic utilities for operations over Cassandra.
 */
public class CassandraUtils {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CassandraUtils.class);

    private final String host;

    private Cluster cluster;

    private Metadata metadata;

    private Session session;

    private CassandraQueryUtils cassandraqueryUtils;

    /**
     * Generic contructor of CassandraUtils.
     */
    public CassandraUtils() {
        this.host = System.getProperty("CASSANDRA_HOST", "127.0.0.1");
    }


    /**
     * Connect to Cassandra host.
     */
    public void connect() {
        buildCluster();
        this.cassandraqueryUtils = new CassandraQueryUtils();
        this.metadata = this.cluster.getMetadata();
        LOGGER.debug("Connected to cluster (" + host + "): "
                + metadata.getClusterName() + "\n");
        this.session = this.cluster.connect();
    }

    /**
     * Execute a query over Cassandra.
     *
     * @param query the query
     * @return ResultSet result set
     */
    public ResultSet executeQuery(String query) {
        return this.session.execute(query);
    }

    /**
     * Execute a list of queries over Cassandra.
     *
     * @param queriesList the queries list
     */
    public void executeQueriesList(List<String> queriesList) {

        for (String query : queriesList) {
            this.session.execute(query);
        }
    }

    /**
     * Reconnect to Cassandra host.
     */
    public void reconnect() {
        metadata = cluster.getMetadata();
        LOGGER.debug("Connected to cluster (" + host + "): "
                + metadata.getClusterName() + "\n");
        this.session = this.cluster.connect();
    }

    /**
     * Disconnect from Cassandra host.
     *
     * @throws DBException  DBException
     */
    public void disconnect() throws DBException {
        if (this.session == null) {
            throw new DBException("The Cassandra is null");
        }
        if (this.cluster.isClosed()) {
            throw new DBException("The cluster has been closed");
        }
        this.session.close();
        this.cluster.close();
    }

    /**
     * Get the metadata of the Cassandra Cluster.
     *
     * @return Metadata metadata
     * @throws DBException the db exception
     */
    public Metadata getMetadata() throws DBException {
        if (!this.cluster.isClosed()) {
            this.metadata = cluster.getMetadata();
            return this.metadata;
        } else {
            throw new DBException("The cluster has been closed");
        }
    }

    /**
     * Build a Cassandra cluster.
     */
    public void buildCluster() {
        this.cluster = Cluster.builder().addContactPoint(this.host).build();
        this.cluster.getConfiguration().getQueryOptions()
                .setConsistencyLevel(ConsistencyLevel.ONE);

    }

    /**
     * Get the cassandra session.
     *
     * @return Session
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Create a keyspace in Cassandra.
     *
     * @param keyspace the keyspace
     */
    public void createKeyspace(String keyspace) {
        Map<String, String> replicationSimpleOneExtra = new HashMap<>();
        replicationSimpleOneExtra.put("'class'", "'SimpleStrategy'");
        replicationSimpleOneExtra.put("'replication_factor'", "1");
        String query = this.cassandraqueryUtils.createKeyspaceQuery(true,
                keyspace, this.cassandraqueryUtils
                        .createKeyspaceReplication(replicationSimpleOneExtra),
                "");
        LOGGER.debug(query);
        executeQuery(query);
    }

    /**
     * Create a table in Cassandra.
     *
     * @param table  the table name
     * @param colums columns
     * @param pk     the pk
     */
    public void createTableWithData(String table, Map<String, String> colums, ArrayList<String> pk) {
        String query = this.cassandraqueryUtils.createTable(table, colums, pk);
        LOGGER.debug(query);
        executeQuery(query);
    }

    /**
     * Insert data in a keyspace.
     *
     * @param table  the table
     * @param fields the fields
     */
    public void insertData(String table, Map<String, Object> fields) {
        String query = this.cassandraqueryUtils.insertData(table, fields);
        LOGGER.debug(query);
        executeQuery(query);
    }

    /**
     * Checks if a keyspace exists in Cassandra.
     *
     * @param keyspace the keyspace
     * @param showLog  if log should be displayed
     * @return boolean boolean
     */
    public boolean existsKeyspace(String keyspace, boolean showLog) {
        this.metadata = cluster.getMetadata();
        if (!(this.metadata.getKeyspaces().isEmpty())) {
            for (KeyspaceMetadata k : metadata.getKeyspaces()) {
                if (k.getName().equals(keyspace)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get a list of the existing keyspaces in Cassandra.
     *
     * @return {@code List<String>}
     */
    public List<String> getKeyspaces() {
        ArrayList<String> result = new ArrayList<String>();
        this.metadata = this.cluster.getMetadata();
        if (!(metadata.getKeyspaces().isEmpty())) {
            for (KeyspaceMetadata k : this.metadata.getKeyspaces()) {
                result.add(k.getName());
            }
        }
        return result;
    }

    /**
     * Drop a keyspace in Cassandra.
     *
     * @param keyspace the keyspace
     */
    public void dropKeyspace(String keyspace) {
        executeQuery(this.cassandraqueryUtils
                .dropKeyspaceQuery(false, keyspace));
    }

    /**
     * Drop a keyspace in Cassandra.
     *
     * @param ifExists to verify if exists
     * @param keyspace the keyspace
     */
    public void dropKeyspace(boolean ifExists, String keyspace) {
        executeQuery(this.cassandraqueryUtils.dropKeyspaceQuery(ifExists,
                keyspace));
    }

    /**
     * Use a keyspace in Cassandra.
     *
     * @param keyspace the keyspace
     */
    public void useKeyspace(String keyspace) {
        executeQuery(this.cassandraqueryUtils.useQuery(keyspace));
    }

    /**
     * Checks if a keyspace contains an especific table.
     *
     * @param keyspace the keyspace
     * @param table    the table
     * @param showLog  if show log
     * @return boolean boolean
     */
    public boolean existsTable(String keyspace, String table, boolean showLog) {
        this.metadata = this.cluster.getMetadata();

        if (!(this.metadata.getKeyspace(keyspace).getTables().isEmpty())) {
            for (TableMetadata t : this.metadata.getKeyspace(keyspace).getTables()) {
                if (t.getName().equals(table)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get tables of a keyspace.
     *
     * @param keyspace the keyspace
     * @return {@code List<String>}
     */
    public List<String> getTables(String keyspace) {
        ArrayList<String> result = new ArrayList<String>();
        this.metadata = this.cluster.getMetadata();
        if ((!existsKeyspace(keyspace, false)) || (this.metadata.getKeyspace(keyspace).getTables().isEmpty())) {
            return result;
        }
        for (TableMetadata t : this.metadata.getKeyspace(keyspace).getTables()) {
            result.add(t.getName());
        }
        return result;
    }

    /**
     * Drop a table of a keyspace.
     *
     * @param table the table name
     */
    public void dropTable(String table) {
        executeQuery(this.cassandraqueryUtils.dropTableQuery(false, table));
    }

    /**
     * Truncate a table of a keyspace.
     *
     * @param table the table name
     */
    public void truncateTable(String table) {
        executeQuery(this.cassandraqueryUtils.truncateTableQuery(false, table));
    }


    /**
     * Load a {@code keyspace} in Cassandra using the CQL sentences in the
     * script path. The script is executed if the keyspace does not exists in
     * Cassandra.
     *
     * @param keyspace The name of the keyspace.
     * @param path     The path of the CQL script.
     */
    public void loadTestData(String keyspace, String path) {
        KeyspaceMetadata md = session.getCluster().getMetadata().getKeyspace(keyspace);
        if (md == null) {
            LOGGER.info("Creating keyspace {} using {}", keyspace, path);
            createKeyspace(keyspace);
        }
        List<String> scriptLines = loadScript(path);
        LOGGER.info("Executing {} lines ", scriptLines.size());
        for (String cql : scriptLines) {
            ResultSet result = session.execute(cql);
            LOGGER.debug("Executing: {}", cql);
        }
        LOGGER.info("Using existing keyspace {}", keyspace);
    }

    /**
     * Load the lines of a CQL script containing one statement per line into a
     * list. l
     *
     * @param path The path of the CQL script.
     * @return The contents of the script.
     */
    public static List<String> loadScript(String path) {
        List<String> result = new ArrayList<String>();
        URL url = CassandraUtils.class.getResource(path);
        LOGGER.debug(url.toString());
        LOGGER.info("Loading script from: " + url);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() > 0 && !line.startsWith("#")) {
                    result.add(line);
                }
            }
        } catch (IOException e) {
            LOGGER.error("IO Exception loading a cql script", e);
        }
        return result;
    }
}
