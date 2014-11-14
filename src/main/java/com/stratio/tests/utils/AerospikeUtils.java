package com.stratio.tests.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Host;
import com.aerospike.client.Key;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;

import cucumber.api.DataTable;
/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 *
 */
public class AerospikeUtils {
    private static final int DEFAULT_TIMEOUT = 50;
    private static final Logger LOGGER = LoggerFactory.getLogger(AerospikeUtils.class);
    private final String host;
    private final Integer port;
    private AerospikeClient client;
/**
 * Constructor Aerospike utils.
 */
    public AerospikeUtils() {
        this.host = System.getProperty("AEROSPIKE_HOST", "127.0.0.1");
        this.port = Integer.parseInt(System.getProperty("AEROSPIKE_PORT", "3000"));
    }
/**
 * Connect to aerospike host.
 */
    public void connect() {
        Host[] hosts = new Host[] { new Host(this.host, this.port) };
        try {
            client = new AerospikeClient(new ClientPolicy(), hosts);
            LOGGER.debug("Initializing Aerospike client");
        } catch (AerospikeException e) {
            LOGGER.error("Unable to connect to Aerospike", e);
        }
    }
/**
 * Check if it has connection with the aerospike server.
 * @return
 */
    public boolean isConnected() {
        return client.isConnected();
    }
/**
 * Disconnect of the Aerospike Server.
 */
    public void disconnect() {
        client.close();
    }
/**
 * Insert data in Aerospike table..
 * @param nameSpace
 * @param tableName
 * @param table
 */
    public void insertFromDataTable(String nameSpace, String tableName, DataTable table) {
        // Primero comprobamos el numero de filas del datable
        WritePolicy writePolicy = new WritePolicy();
        writePolicy.timeout = DEFAULT_TIMEOUT;
        List<List<String>> tableAsList = table.raw();
        // (Se resta uno porque la primera fila indica los columnNames)
        int iKey = tableAsList.size();

        for (int i = 1; i < iKey; i++) {
            try {
                Key key = new Key(nameSpace, tableName, "MyKey" + i);
                Bin[] bins = createBins(tableAsList.get(0), tableAsList.get(i));
                client.add(writePolicy, key, bins[0]);
                client.add(writePolicy, key, bins[1]);
            } catch (AerospikeException e) {
                if (e.getMessage().contains("4:")) {
                    LOGGER.error("ERROR : " + e.getMessage());
                    LOGGER.error("It is possible that you forget to create the namespace.");
                    LOGGER.error("Go to \"etc/aerospike/aerospike.conf\" and create a the namespace.");
                } else {
                    LOGGER.error("ERROR : ", e);
                }
            }
        }
    }

    private Bin[] createBins(List<String> columnNames, List<String> row) {
        Bin[] bins = new Bin[columnNames.size()];
        for (int i = 0; i < columnNames.size(); i++) {
            bins[i] = new Bin(columnNames.get(i).toString(), row.get(i).toString());
        }
        return bins;
    }
/**
 * Read a table from aerospike.
 * @param nameSpace
 * @param table
 * @return
 */
    public RecordSet readTable(String nameSpace, String table) {
        Statement stmt = new Statement();
        stmt.setNamespace(nameSpace);
        stmt.setSetName(table);
        RecordSet rs = null;
        try {
            rs = client.query(null, stmt);
        } catch (AerospikeException e) {
            LOGGER.error("ERROR : " + e.getMessage());
        }
        stmt = null;
        return rs;
    }

}
