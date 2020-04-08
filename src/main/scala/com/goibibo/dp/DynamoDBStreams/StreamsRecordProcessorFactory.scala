package com.goibibo.dp.DynamoDBStreams

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.{IRecordProcessor, IRecordProcessorFactory}
import org.apache.kafka.clients.producer.KafkaProducer

//remove if not needed

class StreamsRecordProcessorFactory(private val dynamoDBClient: AmazonDynamoDB, tableName: String,
                                    private val topicName: String, producer: KafkaProducer[String, String])
  extends IRecordProcessorFactory {

  override def createProcessor(): IRecordProcessor =
    new StreamsRecordProcessor(dynamoDBClient, tableName, topicName, producer)

}