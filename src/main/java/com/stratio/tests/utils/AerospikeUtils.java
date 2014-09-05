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

public class AerospikeUtils {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AerospikeUtils.class);
	private final String host;
	private final Integer port;
	private AerospikeClient client;

	public AerospikeUtils() {
		this.host = System.getProperty("AEROSPIKE_HOST", "127.0.0.1");
		this.port = Integer.parseInt(System.getProperty("AEROSPIKE_PORT", "3000"));
	}

	public void connect() {
		Host[] hosts = new Host[] { new Host(this.host, this.port) };
		try {
			client = new AerospikeClient(new ClientPolicy(), hosts);
			LOGGER.debug("Initializing Aerospike client");
		} catch (AerospikeException e) {
			LOGGER.error("Unable to connect to Aerospike", e);
		}
	}

	public boolean isConnected() {
		if (client.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	public void disconnect() {
		client.close();
	}

	public void insertFromDataTable(String NameSpace, String tableName,
			DataTable table) {
		// Primero comprobamos el numero de filas del datable
		WritePolicy writePolicy = new WritePolicy();
		writePolicy.timeout = 50;
		List<List<String>> table_as_list = table.raw();
		int iKey = table_as_list.size();// (Se resta uno porque la primera fila
										// indica los columnNames)
		for (int i = 1; i < iKey; i++) {
			try {
				Key key = new Key(NameSpace, tableName, "MyKey" + i);
				Bin[] bins = createBins(table_as_list.get(0),
						table_as_list.get(i));
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
			bins[i] = new Bin(columnNames.get(i).toString(), row.get(i)
					.toString());
		}
		return bins;
	}

	public RecordSet readTable(String NameSpace, String table) {
		Statement stmt = new Statement();
		stmt.setNamespace(NameSpace);
		stmt.setSetName(table);
		RecordSet rs = null;
		try {
			rs = client.query(null, stmt);
		} catch (AerospikeException e) {
			LOGGER.error("ERROR : " + e.getMessage());
		}
		return rs;
	}

}
