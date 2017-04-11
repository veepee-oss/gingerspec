@Library('libpipelines@master') _

hose {
    EMAIL = 'qa'
    LANG = 'java'
    MODULE = 'bdt'
    REPOSITORY = 'github.com/bdt'
    SLACKTEAM = 'stratioqa'
    DEVTIMEOUT = 30
    RELEASETIMEOUT = 30
    MAXITRETRIES = 2
    FOSS = true

    ITSERVICES = [
        ['ZOOKEEPER': [
           'image': 'jplock/zookeeper:3.5.2-alpha',
           'env': [
                 'zk_id=1'],
           'sleep': 30,
           'healthcheck': 2181]],
        ['MONGODB': [
           'image': 'stratio/mongo:3.0.4']],
        ['ELASTICSEARCH': [
           'image': 'elasticsearch:2.0.2',
           'env': [
                 'ES_JAVA_OPTS="-Des.cluster.name=%%JUID -Des.network.host=%%OWNHOSTNAME"'],
           'sleep': 10,
           'healthcheck': 9300]],
        ['CASSANDRA': [
           'image': 'stratio/cassandra-lucene-index:3.0.7.3',
           'volumes':[
                 'jts:1.14.0'],
           'env': [
                 'MAX_HEAP=256M'],
           'sleep': 30,
           'healthcheck': 9042]],
        ['KAFKA': [
           'image': 'confluent/kafka:0.10.0.0-cp1',
           'env': [
                 'KAFKA_ZOOKEEPER_CONNECT=%%ZOOKEEPER:2181',
                 'KAFKA_ADVERTISED_HOST_NAME=%%OWNHOSTNAME',
                 'KAFKA_DELETE_TOPIC_ENABLE=true'],
           'sleep': 30,       
           'healthcheck': 9300]],
	['CHROME': [
	   'image': 'stratio/selenium-chrome:48',
           'volumes': [
		 '/dev/shm:/dev/shm'],
           'env': [
		 'SELENIUM_GRID=selenium.cd','ID=%%JUID']]],
        ['UBUNTU': [
           'image': 'stratio/ubuntu-base:16.04',
           'ssh': true]],
    ]
    
    ITPARAMETERS = """
	| -DSELENIUM_GRID=selenium.cd:4444 
	| -DFORCE_BROWSER=chrome_48%%JUID
        | -DMONGO_HOST=%%MONGODB
        | -DCASSANDRA_HOST=%%CASSANDRA#0
        | -DES_NODE=%%ELASTICSEARCH#0
        | -DES_CLUSTER=%%JUID
        | -DZOOKEEPER_HOSTS=%%ZOOKEEPER:2181
        | -DSECURIZED_ZOOKEEPER=false
        | -DWAIT=1
        | -DAGENT_LIST=1,2
        | -DKAFKA_HOSTS=%%KAFKA:9092
        | -DSSH=%%UBUNTU
        | -DSLEEPTEST=1""".stripMargin().stripIndent()
    
    DEV = { config ->        
        doCompile(config)

        parallel(UT: {
            doUT(config)
        }, IT: {
            doIT(config)
        }, failFast: config.FAILFAST)

        doPackage(config)
    
        parallel(DOC: {
            doDoc(config)
        }, QC: {
            doStaticAnalysis(config)
        }, DEPLOY: {
            doDeploy(config)
        }, failFast: config.FAILFAST)

     }
}
