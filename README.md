# DynamoDBStreams-Connector


Supervisor Kafka Setup:
```bash

[program:kafka]
command=bash -c "JMX_PORT=17264 KAFKA_HEAP_OPTS='-Xms1024M -Xmx3072M' /home/ssm-user/kafka/kafka_2.11-2.2.1/bin/kafka-server-start.sh /home/ssm-user/kafka/kafka_2.11-2.2.1/config/server.properties"
directory=/home/anshul.bansal
user=root
autostart=true
autorestart=true
stdout_logfile=/home/ssm-user/logs/kafka/stdout.log
stderr_logfile=/home/ssm-user/logs/kafka/stderr.log
environment = JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.242.b08-0.amzn2.0.1.x86_64
```


Zookeeper Setup:
```bash
[program:zookeeper]
command=bash -c "/home/ssm-user/kafka/kafka_2.11-2.2.1/bin/zookeeper-server-start.sh /home/ssm-user/kafka/kafka_2.11-2.2.1/config/zookeeper.properties"
directory=/home/anshul.bansal
user=root
autostart=true
autorestart=true
stdout_logfile=/home/ssm-user/logs/zookeeper/stdout.log
stderr_logfile=/home/ssm-user/logs/zookeeper/stderr.log
environment = JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.242.b08-0.amzn2.0.1.x86_64
```

DynamoDBstreams run command: 

```bash
java -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+CMSScavengeBeforeRemark -XX:+DisableExplicitGC -Djava.awt.headless=true -Xmx3g -Xms1g -DDynamoDBStreams.kafka.write-topic="covid_user_details" \
-DDynamoDBStreams.kafka.brokers="b-2.covid19-kafka.wg14nx.c2.kafka.ap-south-1.amazonaws.com:9092,b-1.covid19-kafka.wg14nx.c2.kafka.ap-south-1.amazonaws.com:9092" \
-DDynamoDBStreams.streamsARN="arn:aws:dynamodb:ap-south-1:726941307807:table/covid_user_details/stream/2020-04-02T11:58:53.435" \
-DDynamoDBStreams.DynamoDBTableName="covid_user_details" \
-DDynamoDBStreams.applicationName="kcl-adapter" \
-DDynamoDBStreams.workerID="streams-worker-prod" \
-cp /home/anshul.bansal/DynamoDBStreamsConnector/target/scala-2.11/DynamoDBStreams-Connector-assembly-0.1.jar \
com.goibibo.dp.DynamoDBStreams.Main > /home/anshul.bansal/logs/Streams_covid_user_details_$(date '+%Y%m%d-%H').logs 2>&1
```
