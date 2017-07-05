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

import com.datastax.driver.core.*;
import com.stratio.qa.exceptions.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
     * @param query
     * @return ResultSet
     */
    public ResultSet executeQuery(String query) {
        return this.session.execute(query);
    }

    /**
     * Execute a list of queries over Cassandra.
     *
     * @param queriesList
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
     * @throws DBException
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
     * @return Metadata
     * @throws DBException
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
     * @param keyspace
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
     * @param table
     * @param colums
     */
    public void createTableWithData(String table, Map<String, String> colums, ArrayList<String> pk) {
        String query = this.cassandraqueryUtils.createTable(table, colums, pk);
        LOGGER.debug(query);
        executeQuery(query);
    }

    /**
     * Insert data in a keyspace.
     *
     * @param table
     * @param fields
     */
    public void insertData(String table, Map<String, Object> fields) {
        String query = this.cassandraqueryUtils.insertData(table, fields);
        LOGGER.debug(query);
        executeQuery(query);
    }

    /**
     * Checks if a keyspace exists in Cassandra.
     *
     * @param keyspace
     * @param showLog
     * @return boolean
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
     * @param keyspace
     */
    public void dropKeyspace(String keyspace) {
        executeQuery(this.cassandraqueryUtils
                .dropKeyspaceQuery(false, keyspace));
    }

    /**
     * Drop a keyspace in Cassandra.
     *
     * @param ifExists
     * @param keyspace
     */
    public void dropKeyspace(boolean ifExists, String keyspace) {
        executeQuery(this.cassandraqueryUtils.dropKeyspaceQuery(ifExists,
                keyspace));
    }

    /**
     * Use a keyspace in Cassandra.
     *
     * @param keyspace
     */
    public void useKeyspace(String keyspace) {
        executeQuery(this.cassandraqueryUtils.useQuery(keyspace));
    }

    /**
     * Checks if a keyspace contains an especific table.
     *
     * @param keyspace
     * @param table
     * @param showLog
     * @return boolean
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
     * @param keyspace
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
     * @param table
     */
    public void dropTable(String table) {
        executeQuery(this.cassandraqueryUtils.dropTableQuery(false, table));
    }

    /**
     * Truncate a table of a keyspace.
     *
     * @param table
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
                url.openStream(), "UTF8"))) {
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
