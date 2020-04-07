# DynamoDBStreams-Connector


Supervisor Kafka Setup:
```bash

[program:kafka]
command=bash -c "JMX_PORT=17264 KAFKA_HEAP_OPTS='-Xms1024M -Xmx3072M' /home/anshul.bansal/kafka_2.12-2.4.1/bin/kafka-server-start.sh /home/anshul.bansal/kafka_2.12-2.4.1/config/server.properties"
directory=/home/anshul.bansal
user=root
autostart=true
autorestart=true
stdout_logfile=/home/anshul.bansal/log/kafka/stdout.log
stderr_logfile=/home/anshul.bansal/log/kafka/stderr.log
environment = JAVA_HOME=/usr/java/jdk1.8.0_92
```


Zookeeper Setup:
```bash
[program:zookeeper]
command=bash -c "/home/anshul.bansal/kafka_2.12-2.4.1/bin/zookeeper-server-start.sh /home/anshul.bansal/kafka_2.12-2.4.1/config/zookeeper.properties"
directory=/home/anshul.bansal
user=root
autostart=true
autorestart=true
stdout_logfile=/home/anshul.bansal/log/zookeeper/stdout.log
stderr_logfile=/home/anshul.bansal/log/zookeeper/stderr.log
environment = JAVA_HOME=/usr/java/jdk1.8.0_92
```

DynamoDBstreams run command: 

```bash
java -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+CMSScavengeBeforeRemark -XX:+DisableExplicitGC -Djava.awt.headless=true -Xmx3g -Xms1g -DDynamoDBStreams.kafka.write-topic="user-activities-test" \
-DDynamoDBStreams.kafka.brokers="localhost:9092" \
-DDynamoDBStreams.streamsARN="arn:aws:dynamodb:ap-south-1:017357459259:table/user-activities/stream/2018-07-23T08:28:00.483" \
-DDynamoDBStreams.DynamoDBTableName="user-activities" \
-cp /home/anshul.bansal/DynamoDBStreamsConnector/target/scala-2.11/DynamoDBStreams-Connector-assembly-0.1.jar com.goibibo.dp.DynamoDBStreams.Main > test.log
```
