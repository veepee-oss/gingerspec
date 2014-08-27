package com.stratio.tests.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;

public class CassandraUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraUtils.class);

  private Cluster cluster;

  private final String host;

  private Metadata metadata;

  private static Session session;

  private QueryUtils queryUtils;

  public CassandraUtils() {
    this.host = System.getProperty("CASSANDRA_HOST", "127.0.0.1");
    buildCluster();
    queryUtils = new QueryUtils();
    metadata = cluster.getMetadata();
    LOGGER.debug("Connected to cluster (" + host + "): " + metadata.getClusterName() + "\n");
    session = cluster.connect();

  }

  public ResultSet executeQuery(String query) {

    return session.execute(query);
  }

  public void executeQueriesList(List<String> queriesList) {

    for (String query : queriesList) {
      session.execute(query);
    }
  }

  public void reconnect() {
    metadata = cluster.getMetadata();
    LOGGER.debug("Connected to cluster (" + host + "): " + metadata.getClusterName() + "\n");
    session = cluster.connect();
  }

  public void disconnect() {
    session.close();
  }

  public Metadata getMetadata() {
    metadata = cluster.getMetadata();
    return metadata;
  }

  public void buildCluster() {
    this.cluster = Cluster.builder().addContactPoint(host).build();
    this.cluster.getConfiguration().getQueryOptions().setConsistencyLevel(ConsistencyLevel.ONE);

  }

  public Session getSession() {
    return session;
  }

  public void createKeyspace(String keyspace) {
    Hashtable<String, String> replicationSimpleOneExtra = new Hashtable<String, String>();
    replicationSimpleOneExtra.put("'class'", "'SimpleStrategy'");
    replicationSimpleOneExtra.put("'replication_factor'", "1");
    String query =
        queryUtils.createKeyspaceQuery(true, keyspace,
            queryUtils.createKeyspaceReplication(replicationSimpleOneExtra), "");
    LOGGER.debug(query);
    executeQuery(query);
  }

  public boolean existsKeyspace(String keyspace, boolean showLog) {
    metadata = cluster.getMetadata();
    if (metadata.getKeyspaces().isEmpty())
      return false;
    for (KeyspaceMetadata k : metadata.getKeyspaces()) {
      if (showLog)
        LOGGER.debug(k.getName());
      if (k.getName().equals(keyspace)) {
        return true;
      }
    }
    return false;
  }

  public ArrayList<String> getKeyspaces(){
	  ArrayList<String> result = new ArrayList<String>();
	  metadata = cluster.getMetadata();
	  if (metadata.getKeyspaces().isEmpty())
	    return result;
	  for (KeyspaceMetadata k : metadata.getKeyspaces()) {
		  result.add(k.getName());
	  }
	  return result;
   }
  
  public void dropKeyspace(String keyspace) {
    executeQuery(queryUtils.dropKeyspaceQuery(false, keyspace));
  }

  public void dropKeyspace(boolean ifExists, String keyspace) {
    executeQuery(queryUtils.dropKeyspaceQuery(ifExists, keyspace));
  }

  public void useKeyspace(String keyspace) {
    executeQuery(queryUtils.useQuery(keyspace));
  }

  public boolean existsTable(String keyspace, String table, boolean showLog) {
    metadata = cluster.getMetadata();

    if (metadata.getKeyspace(keyspace).getTables().isEmpty())
      return false;
    for (TableMetadata t : metadata.getKeyspace(keyspace).getTables()) {
      if (showLog) {
        if (t.getName() != null)
          LOGGER.debug(t.getName());
      }
      if (t.getName().equals(table)) {
        return true;
      }
    }
    return false;
  }

  public ArrayList<String> getTables(String keyspace){
	  ArrayList<String> result = new ArrayList<String>();
	  metadata = cluster.getMetadata();
	  if (metadata.getKeyspace(keyspace).getTables().isEmpty())
	      return result;
	  for (TableMetadata t : metadata.getKeyspace(keyspace).getTables()) {
		  result.add(t.getName());
	  }
	  return result;
   }
  
  public void dropTable(String keyspace, String table) {
    // se elimina la table
    executeQuery(queryUtils.dropTableQuery(false, table));

  }

  /**
   * Load a {@code keyspace} in Cassandra using the CQL sentences in the script path. The script is
   * executed if the keyspace does not exists in Cassandra.
   * 
   * @param keyspace The name of the keyspace.
   * @param path The path of the CQL script.
   */
  public void loadTestData(String keyspace, String path) {
    KeyspaceMetadata metadata = session.getCluster().getMetadata().getKeyspace(keyspace);
    if (metadata == null) {
      LOGGER.info("Creating keyspace " + keyspace + " using " + path);
      List<String> scriptLines = loadScript(path);
      LOGGER.info("Executing " + scriptLines.size() + " lines");
      for (String cql : scriptLines) {
        ResultSet result = session.execute(cql);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Executing: " + cql + " -> " + result.toString());
        }
      }
    }
    LOGGER.info("Using existing keyspace " + keyspace);
  }

  /**
   * Load the lines of a CQL script containing one statement per line into a list.
   * l
   * @param path The path of the CQL script.
   * @return The contents of the script.
   */
  public static List<String> loadScript(String path) {
    List<String> result = new ArrayList<String>();
    URL url = CassandraUtils.class.getResource(path);
    LOGGER.debug(url.toString());
    LOGGER.info("Loading script from: " + url);
    try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.length() > 0 && !line.startsWith("#")) {
          result.add(line);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

}
