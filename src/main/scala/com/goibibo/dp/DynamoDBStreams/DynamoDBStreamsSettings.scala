package com.goibibo.dp.DynamoDBStreams

import com.typesafe.config.{Config, ConfigFactory}

object DynamoDBStreamsSettings {


    val conf: Config = ConfigFactory.load.getConfig("DynamoDBStreams")
    val streamsARN: String = conf.getString("streamsARN")
    val DynamoDBTableName: String = conf.getString("DynamoDBTableName")
    val bootstrapServers: String = conf.getString("kafka.brokers")
    val compressionType: String = conf.getString("kafka.compression-type")
    val batchSize: String = conf.getString("kafka.batch-size")
    val lingerMS: String = conf.getString("kafka.linger-ms")
    val writeTopic: String = conf.getString("kafka.write-topic")
}
