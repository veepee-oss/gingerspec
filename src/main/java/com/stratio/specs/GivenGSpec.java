package com.stratio.specs;

import com.auth0.jwt.JWTSigner;
import com.ning.http.client.cookie.Cookie;
import com.stratio.exceptions.DBException;
import com.stratio.tests.utils.RemoteSSHConnection;
import com.stratio.tests.utils.ThreadProperty;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.stratio.assertions.Assertions.assertThat;

/**
 * Generic Given Specs.
 */
public class GivenGSpec extends BaseGSpec {

    public static final int PAGE_LOAD_TIMEOUT = 120;
    public static final int IMPLICITLY_WAIT = 10;
    public static final int SCRIPT_TIMEOUT = 30;

    /**
     * Generic constructor.
     *
     * @param spec
     */
    public GivenGSpec(CommonG spec) {
        this.commonspec = spec;

    }

    /**
     * Create a basic Index.
     *
     * @param index_name index name
     * @param table      the table where index will be created.
     * @param column     the column where index will be saved
     * @param keyspace   keyspace used
     * @throws Exception
     */
    @Given("^I create a Cassandra index named '(.+?)' in table '(.+?)' using magic_column '(.+?)' using keyspace '(.+?)'$")
    public void createBasicMapping(String index_name, String table, String column, String keyspace) throws Exception {
        commonspec.getLogger().debug("Creating a basic index");
        String query = "CREATE INDEX " + index_name + " ON " + table + " (" + column + ");";
        commonspec.getCassandraClient().executeQuery(query);
    }

    /**
     * Create a Cassandra Keyspace.
     *
     * @param keyspace
     */
    @Given("^I create a Cassandra keyspace named '(.+)'$")
    public void createCassandraKeyspace(String keyspace) {
        commonspec.getLogger().debug("Creating a Cassandra keyspace");
        commonspec.getCassandraClient().createKeyspace(keyspace);
    }

    /**
     * Connect to cluster.
     *
     * @param clusterType DB type (Cassandra|Mongo|Elasticsearch)
     * @param url         url where is started Cassandra cluster
     */
    @Given("^I connect to '(Cassandra|Mongo|Elasticsearch)' cluster at '(.+)'$")
    public void connect(String clusterType, String url) throws DBException, UnknownHostException {
        commonspec.getLogger().debug("Connecting to " + clusterType + " cluster", "");
        switch (clusterType) {
            case "Cassandra":
                commonspec.getCassandraClient().buildCluster();
                commonspec.getCassandraClient().connect();
                break;
            case "Mongo":
                commonspec.getMongoDBClient().connect();
                break;
            case "Elasticsearch":
                LinkedHashMap<String, Object> settings_map = new LinkedHashMap<String, Object>();
                settings_map.put("cluster.name", System.getProperty("ES_CLUSTER", "elasticsearch"));
                commonspec.getElasticSearchClient().setSettings(settings_map);
                commonspec.getElasticSearchClient().connect();
                break;
            default:
                throw new DBException("Unknown cluster type");
        }
    }

    /**
     * Create table
     *
     * @param table
     * @param datatable
     * @param keyspace
     * @throws Exception
     */
    @Given("^I create a Cassandra table named '(.+?)' using keyspace '(.+?)' with:$")
    public void createTableWithData(String table, String keyspace, DataTable datatable) {
        try {
            commonspec.getCassandraClient().useKeyspace(keyspace);
            commonspec.getLogger().debug("Starting a table creation", "");
            int attrLength = datatable.getGherkinRows().get(0).getCells().size();
            Map<String, String> columns = new HashMap<String, String>();
            ArrayList<String> pk = new ArrayList<String>();

            for (int i = 0; i < attrLength; i++) {
                columns.put(datatable.getGherkinRows().get(0).getCells().get(i),
                        datatable.getGherkinRows().get(1).getCells().get(i));
                if ((datatable.getGherkinRows().size() == 3) && datatable.getGherkinRows().get(2).getCells().get(i).equalsIgnoreCase("PK")) {
                    pk.add(datatable.getGherkinRows().get(0).getCells().get(i));
                }
            }
            if (pk.isEmpty()) {
                throw new Exception("A PK is needed");
            }
            commonspec.getCassandraClient().createTableWithData(table, columns, pk);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
    }

    /**
     * Insert Data
     *
     * @param table
     * @param datatable
     * @param keyspace
     * @throws Exception
     */
    @Given("^I insert in keyspace '(.+?)' and table '(.+?)' with:$")
    public void insertData(String keyspace, String table, DataTable datatable) {
        try {
            commonspec.getCassandraClient().useKeyspace(keyspace);
            commonspec.getLogger().debug("Starting a table creation", "");
            int attrLength = datatable.getGherkinRows().get(0).getCells().size();
            Map<String, Object> fields = new HashMap<String, Object>();
            for (int e = 1; e < datatable.getGherkinRows().size(); e++) {
                for (int i = 0; i < attrLength; i++) {
                    fields.put(datatable.getGherkinRows().get(0).getCells().get(i), datatable.getGherkinRows().get(e).getCells().get(i));

                }
                commonspec.getCassandraClient().insertData(keyspace + "." + table, fields);

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
    }


    /**
     * Save value for future use.
     * <p>
     * If element is a jsonpath expression (i.e. $.fragments[0].id), it will be
     * applied over the last httpResponse.
     * <p>
     * If element is a jsonpath expression preceded by some other string
     * (i.e. ["a","b",,"c"].$.[0]), it will be applied over this string.
     * This will help to save the result of a jsonpath expression evaluated over
     * previous stored variable.
     *
     * @param position position from a search result
     * @param element  key in the json response to be saved
     * @param envVar   thread environment variable where to store the value
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @Given("^I save element (in position \'(.+?)\' in )?\'(.+?)\' in environment variable \'(.+?)\'$")
    public void saveElementEnvironment(String foo, String position, String element, String envVar) throws Exception {

        Pattern pattern = Pattern.compile("^((.*)(\\.)+)(\\$.*)$");
        Matcher matcher = pattern.matcher(element);
        String json;
        String parsedElement;

        if (matcher.find()) {
            json = matcher.group(2);
            parsedElement = matcher.group(4);
        } else {
            json = commonspec.getResponse().getResponse();
            parsedElement = element;
        }

        String value = commonspec.getJSONPathString(json, parsedElement, position);

        if (value == null) {
            throw new Exception("Element to be saved: " + element + " is null");
        } else {
            this.commonspec.getLogger().debug("Saving element: {} with value: {} in environment variable: {}", new Object[]{element, value, envVar});
            ThreadProperty.set(envVar, value);
        }
    }

    /**
     * Drop all the ElasticSearch indexes.
     */
    @Given("^I drop every existing elasticsearch index$")
    public void dropElasticsearchIndexes() {
        commonspec.getLogger().debug("Dropping es indexes");
        commonspec.getElasticSearchClient().dropAllIndexes();
    }

    /**
     * Drop an specific index of ElasticSearch.
     *
     * @param index
     */
    @Given("^I drop an elasticsearch index named '(.+?)'$")
    public void dropElasticsearchIndex(String index) {
        commonspec.getLogger().debug("Dropping an es index: {}", index);
        commonspec.getElasticSearchClient().dropSingleIndex(index);
    }

    /**
     * Execute a cql file over a Cassandra keyspace.
     *
     * @param filename
     * @param keyspace
     */
    @Given("a Cassandra script with name '(.+?)' and default keyspace '(.+?)'$")
    public void insertDataOnCassandraFromFile(String filename, String keyspace) {
        commonspec.getLogger().debug("Inserting data on cassandra from file");
        commonspec.getCassandraClient().loadTestData(keyspace, "/scripts/" + filename);
    }

    /**
     * Drop a Cassandra Keyspace.
     *
     * @param keyspace
     */
    @Given("^I drop a Cassandra keyspace '(.+)'$")
    public void dropCassandraKeyspace(String keyspace) {
        commonspec.getLogger().debug("Dropping a Cassandra keyspace", keyspace);
        commonspec.getCassandraClient().dropKeyspace(keyspace);
    }

    /**
     * Create a MongoDB dataBase.
     *
     * @param databaseName
     */
    @Given("^I create a MongoDB dataBase '(.+?)'$")
    public void createMongoDBDataBase(String databaseName) {
        commonspec.getLogger().debug("Creating a database on MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(databaseName);

    }

    /**
     * Drop MongoDB Database.
     *
     * @param databaseName
     */
    @Given("^I drop a MongoDB database '(.+?)'$")
    public void dropMongoDBDataBase(String databaseName) {
        commonspec.getLogger().debug("Creating a database on MongoDB");
        commonspec.getMongoDBClient().dropMongoDBDataBase(databaseName);
    }

    /**
     * Insert data in a MongoDB table.
     *
     * @param dataBase
     * @param tabName
     * @param table
     */
    @Given("^I insert into a MongoDB database '(.+?)' and table '(.+?)' this values:$")
    public void insertOnMongoTable(String dataBase, String tabName, DataTable table) {
        commonspec.getLogger().debug("Inserting data in a database on MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(dataBase);
        commonspec.getMongoDBClient().insertIntoMongoDBCollection(tabName, table);
    }

    /**
     * Truncate table in MongoDB.
     *
     * @param database
     * @param table
     */
    @Given("^I drop every document at a MongoDB database '(.+?)' and table '(.+?)'")
    public void truncateTableInMongo(String database, String table) {
        commonspec.getLogger().debug("Truncating a table in MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(database);
        commonspec.getMongoDBClient().dropAllDataMongoDBCollection(table);
    }

    /**
     * Browse to {@code url} using the current browser.
     *
     * @param path
     * @throws Exception
     */
    @Given("^I browse to '(.+?)'$")
    public void seleniumBrowse(String path) throws Exception {
        assertThat(path).isNotEmpty();

        if (commonspec.getWebHost() == null) {
            throw new Exception("Web host has not been set");
        }

        if (commonspec.getWebPort() == null) {
            throw new Exception("Web port has not been set");
        }

        String webURL = "http://" + commonspec.getWebHost() + commonspec.getWebPort();

        commonspec.getLogger().debug("Browsing to {}{} with {}", webURL, path, commonspec.getBrowserName());
        commonspec.getDriver().get(webURL + path);
        commonspec.setParentWindow(commonspec.getDriver().getWindowHandle());
    }

    /**
     * Set app host and port {@code host, @code port}
     *
     * @param host
     * @param port
     */
    @Given("^My app is running in '([^:]+?)(:.+?)?'$")
    public void setupApp(String host, String port) {
        assertThat(host).isNotEmpty();
        assertThat(port).isNotEmpty();

        if (port == null) {
            port = ":80";
        }

        commonspec.setWebHost(host);
        commonspec.setWebPort(port);
        commonspec.setRestHost(host);
        commonspec.setRestPort(port);

        commonspec.getLogger().debug("Set URL to http://{}{}/", host, port);
    }


    /**
     * Browse to {@code webHost, @code webPort} using the current browser.
     *
     * @param webHost
     * @param webPort
     * @throws MalformedURLException
     */
    @Given("^I set web base url to '([^:]+?)(:.+?)?'$")
    public void setupWeb(String webHost, String webPort) throws MalformedURLException {
        assertThat(webHost).isNotEmpty();
        assertThat(webPort).isNotEmpty();

        if (webPort == null) {
            webPort = ":80";
        }

        commonspec.setWebHost(webHost);
        commonspec.setWebPort(webPort);

        commonspec.getLogger().debug("Set web base URL to http://{}{}", webHost, webPort);
    }

    /**
     * Send requests to {@code restHost @code restPort}.
     *
     * @param restHost
     * @param restPort
     */
    @Given("^I( securely)? send requests to '([^:]+?)(:.+?)?'$")
    public void setupRestClient(String isSecured, String restHost, String restPort) {
        assertThat(restHost).isNotEmpty();
        assertThat(restPort).isNotEmpty();

        String restProtocol = "http://";

        if (isSecured != null) {
            restProtocol = "https://";
        }


        if (restHost == null) {
            restHost = "localhost";
        }

        if (restPort == null) {
            restPort = ":80";
        }

        commonspec.setRestProtocol(restProtocol);
        commonspec.setRestHost(restHost);
        commonspec.setRestPort(restPort);
        commonspec.getLogger().debug("Sending requests to {}{}{}", restProtocol, restHost, restPort);
    }

    /**
     * Maximizes current browser window. Mind the current resolution could break a test.
     */
    @Given("^I maximize the browser$")
    public void seleniumMaximize(String url) {
        commonspec.getDriver().manage().window().maximize();
    }

    /**
     * Switches to a frame/ iframe.
     */
    @Given("^I switch to the iframe on index '(\\d+?)'$")
    public void seleniumSwitchFrame(Integer index) {

        assertThat(commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);

        WebElement elem = commonspec.getPreviousWebElements().getPreviousWebElements().get(index);
        commonspec.getDriver().switchTo().frame(elem);
    }

    /**
     * Switches to a parent frame/ iframe.
     */
    @Given("^I switch to a parent frame$")
    public void seleniumSwitchAParentFrame() {
        commonspec.getDriver().switchTo().parentFrame();
    }

    /**
     * Switches to the frames main container.
     */
    @Given("^I switch to the main frame container$")
    public void seleniumSwitchParentFrame() {
        commonspec.getDriver().switchTo().frame(commonspec.getParentWindow());
    }


    /*
     * Opens a ssh connection to remote host
     *
     * @param remoteHost
     * @param user
     * @param password
     *
     */
    @Given("^I open remote ssh connection to host '(.+?)' with user '(.+?)'( and password '(.+?)')?( using pem file '(.+?)')?$")
    public void openSSHConnection(String remoteHost, String user, String foo, String password, String bar, String pemFile) throws Exception {
        if (pemFile == null) {
            if (password == null) {
                throw new Exception("You have to provide a password or a pem file to be used for connection");
            }
            commonspec.getLogger().debug("Openning remote ssh connection to " + remoteHost + " with user " + user +
                    " and password " + password);
        } else {
            File pem = new File(pemFile);
            if (!pem.exists()) {
                throw new Exception("Pem file: " + pemFile + " does not exist");
            }
            commonspec.getLogger().debug("Openning remote ssh connection to " + remoteHost + " with user " + user +
                    " using pem file " + pemFile);
        }
        commonspec.setRemoteSSHConnection(new RemoteSSHConnection(user, password, remoteHost, pemFile));

    }

    /*
   * Authenticate in a DCOS cluster
   *
   * @param remoteHost
   * @param email
   * @param user
   * @param password
   * @param pemFile
   *
   */
    @Given("^I authenticate in DCOS cluster '(.+?)' with email '(.+?)' with user '(.+?)'( and password '(.+?)')?( using pem file '(.+?)')?$")
    public void authenticateDCOSpem(String remoteHost,String email, String user, String foo, String password, String bar, String pemFile) throws Exception {
        if (pemFile == null) {
                throw new Exception("You have to provide a pem file to be used for connection");
            }
        commonspec.getLogger().debug("Authenticating in DCOS cluster " + remoteHost + " with user " + pemFile);
        File pem = new File(pemFile);
        if (!pem.exists()) {
            throw new Exception("Pem file: " + pemFile + " does not exist");
        }
        commonspec.getLogger().debug("Openning remote ssh connection to " + remoteHost + " with user " +
                " using pem file " + pemFile);
        commonspec.setRemoteSSHConnection(new RemoteSSHConnection(user, password, remoteHost, pemFile));

        commonspec.getRemoteSSHConnection().runCommand("sudo cat /var/lib/dcos/dcos-oauth/auth-token-secret");
        String DCOSsecret = commonspec.getRemoteSSHConnection().getResult().trim();

        final JWTSigner signer = new JWTSigner(DCOSsecret);


        final HashMap<String, Object> claims = new HashMap();
        claims.put("uid", email);

        final String jwt = signer.sign(claims);

        Cookie cookie = new Cookie("dcos-acs-auth-cookie", jwt, false, "", "", 99999, false, false);
        List<Cookie> cookieList = new ArrayList<Cookie>();

        cookieList.add(cookie);

        commonspec.setCookies(cookieList);

    }

    /*
    * Authenticate in a DCOS cluster
    *
    * @param dcosHost
    * @param user
    *
    */
    @Given("^I authenticate in DCOS cluster '(.+?)' with email '(.+?)'$")
    public void authenticateDCOS(String dcosCluster, String user) throws Exception {
        commonspec.getLogger().debug("Authenticating in DCOS cluster " + dcosCluster + " with user " + user);

        commonspec.setRemoteSSHConnection(new RemoteSSHConnection("root", "stratio", dcosCluster, null));
        commonspec.getRemoteSSHConnection().runCommand("cat /var/lib/dcos/dcos-oauth/auth-token-secret");
        String DCOSsecret = commonspec.getRemoteSSHConnection().getResult().trim();

        final JWTSigner signer = new JWTSigner(DCOSsecret);
        final HashMap<String, Object> claims = new HashMap();
        claims.put("uid", user);

        final String jwt = signer.sign(claims);

        Cookie cookie = new Cookie("dcos-acs-auth-cookie", jwt, false, "", "", 99999, false, false);
        List<Cookie> cookieList = new ArrayList<Cookie>();

        cookieList.add(cookie);

        commonspec.setCookies(cookieList);

    }

    /*
     * Copies file/s from remote system into local system
     *
     * @param remotePath
     * @param localPath
     *
     */
    @Given("^I copy '(.+?)' from remote ssh connection and store it in '(.+?)'$")
    public void copyFromRemoteFile(String remotePath, String localPath) throws Exception {
        commonspec.getLogger().debug("Copy remote " + remotePath + " to local " + localPath);
        commonspec.getRemoteSSHConnection().copyFrom(remotePath, localPath);
    }


    /*
     * Copies file/s from local system to remote system
     *
     * @param localPath
     * @param remotePath
     *
     */
    @Given("^I copy '(.+?)' to remote ssh connection in '(.+?)'$")
    public void copyToRemoteFile(String localPath, String remotePath) throws Exception {
        commonspec.getLogger().debug("Copy local " + localPath + " to remote " + remotePath);
        commonspec.getRemoteSSHConnection().copyTo(localPath, remotePath);
    }


    /**
     * Executes the command specified in local system
     *
     * @param command
     **/
    @Given("^I execute command '(.+?)' locally$")
    public void executeLocalCommand(String command) throws Exception {
        commonspec.getLogger().debug("Executing command '" + command + "'");
        commonspec.runLocalCommand(command);
    }

    /**
     * Executes the command specified in remote system
     *
     * @param command
     **/
    @Given("^I execute command '(.+?)' in remote ssh connection( with exit status '(.+?)')?$")
    public void executeCommand(String command, String foo, Integer exitStatus) throws Exception {
        if (exitStatus == null) {
            exitStatus = 0;
        }

        commonspec.getLogger().debug("Executing command '" + command + "'");
        commonspec.getRemoteSSHConnection().runCommand(command);
        commonspec.setCommandResult(commonspec.getRemoteSSHConnection().getResult());
        commonspec.setCommandExitStatus(commonspec.getRemoteSSHConnection().getExitStatus());

        List<String> logOutput = Arrays.asList(commonspec.getCommandResult().split("\n"));
        StringBuffer log = new StringBuffer();
        int logLastLines = 25;
        if (logOutput.size() < 25) {
            logLastLines = logOutput.size();
        }
        for (String s : logOutput.subList(logOutput.size() - logLastLines, logOutput.size())) {
            log.append(s).append("\n");
        }

        commonspec.getLogger().info("Exit status is: {}", commonspec.getRemoteSSHConnection().getExitStatus());
        commonspec.getLogger().info("Command last " + logLastLines + " lines stdout:\n{}", log);
        commonspec.getLogger().debug("Command complete stdout:\n{}", commonspec.getCommandResult());
        Assertions.assertThat(commonspec.getRemoteSSHConnection().getExitStatus()).isEqualTo(exitStatus);
    }


    /**
     * Insert document in a MongoDB table.
     *
     * @param dataBase
     * @param collection
     * @param document
     */
    @Given("^I insert into MongoDB database '(.+?)' and collection '(.+?)' the document from schema '(.+?)'$")
    public void insertOnMongoTable(String dataBase, String collection, String document) throws Exception {
        commonspec.getLogger().debug("Inserting data at {}.{} on MongoDB", dataBase, collection);
        String retrievedDoc = commonspec.retrieveData(document, "json");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(dataBase);
        commonspec.getMongoDBClient().insertDocIntoMongoDBCollection(collection, retrievedDoc);
    }


    /**
     * Get all opened windows and store it.
     */
    @Given("^new window is opened$")
    public void seleniumGetwindows() {
        commonspec.getLogger().debug("Getting number of opened windows");
        Set<String> wel = commonspec.getDriver().getWindowHandles();

        Assertions.assertThat(wel).as("Element count doesnt match").hasSize(2);
    }


    /**
     * Connect to zookeeper.
     *
     * @param zookeeperHosts as host:port (comma separated)
     */
    @Given("^I connect to zk cluster at '(.+)'$")
    public void connectToZk(String zookeeperHosts) {
        commonspec.getLogger().debug("Connecting to zookeeper at " + zookeeperHosts);
        commonspec.getZookeeperClient().setZookeeperConnection(zookeeperHosts, 3000);
        commonspec.getZookeeperClient().connectZk();
    }
    /**
     * Connect to Kafka.
     * @param zkHost
     * @param zkPort
     * @param zkPath
     */
    @Given("^I connect to kafka cluster at '(.+)':'(.+)' using path '(.+)'$")
    public void connectKafka(String zkHost, String zkPort, String zkPath) throws UnknownHostException {
        commonspec.getLogger().debug("Connecting to " + zkHost + " cluster");
        if (System.getenv("DCOS_CLUSTER") != null) {
            commonspec.getKafkaUtils().setZkHost(zkHost, zkPort, zkPath);
        } else {
            commonspec.getKafkaUtils().setZkHost(zkHost, zkPort, "dcos-service-" + zkPath);
        }
        commonspec.getKafkaUtils().connect();
    }

}
