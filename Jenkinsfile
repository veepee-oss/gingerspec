@Library('libpipelines@feature/multibranch') _

hose {
    EMAIL = 'qa'
    LANG = 'java'
    SHORTMODULE = 'qa'
    SLACKTEAM = 'stratioqa'
    DEVTIMEOUT = 30
    RELEASETIMEOUT = 30
    MAXITRETRIES = 2

    ITSERVICES = [
        ['ZOOKEEPER': [
           'image': 'confluent/zookeeper:3.4.6-cp1',
           'env': [
                 'zk_id=1'],
           'sleep': 10]],
        ['MONGODB': [
           'image': 'stratio/mongo:3.0.4']],
        ['ELASTICSEARCH': [
           'image': 'elasticsearch:2.0.2',
           'env': [
                 'ES_JAVA_OPTS="-Des.cluster.name=%%JUID -Des.network.host=%%OWNHOSTNAME"'],
           'sleep': 10]],
        ['CASSANDRA': [
           'image': 'stratio/cassandra-lucene-index:3.0.7.3',
           'volumes':[
                 'jts:1.14.0'],
           'env': [
                 'MAX_HEAP=256M'],
           'sleep': 10]],
        ['KAFKA': [
           'image': 'confluent/kafka:0.10.0.0-cp1',
           'env': [
                 'KAFKA_ZOOKEEPER_CONNECT=%%ZOOKEEPER:2181',
                 'KAFKA_ADVERTISED_HOST_NAME=%%OWNHOSTNAME']]],
        ['UBUNTU': [
           'image': 'stratio/ubuntu-base:16.04',
           'ssh': true]],
    ]
    
    ITPARAMETERS = """
        | -DMONGO_HOST=%%MONGODB
        | -DCASSANDRA_HOST=%%CASSANDRA#0
        | -DES_NODE=%%ELASTICSEARCH#0
        | -DES_CLUSTER=%%JUID
        | -DZOOKEEPER_HOSTS=%%ZOOKEEPER:2181
        | -DKAFKA_HOSTS=%%KAFKA:9092
        | -DSSH=%%UBUNTU
        | -DSLEEPTEST=1""".stripMargin().stripIndent()
    
    DEV = { conf ->        
        doCompile(conf)

        parallel(UT: {
            doUT(conf)
        }, IT: {
            doIT(conf)
        }, failFast: conf.FAILFAST)

        doPackage(conf)
    
        parallel(DOC: {
            doDoc(conf)
        }, QC: {
            doStaticAnalysis(conf)
        }, DEPLOY: {
            doDeploy(conf)
        }, failFast: conf.FAILFAST)

     }

    RELEASE = { conf ->
        doDeploy(conf)
        doDoc(conf)
    }
}
