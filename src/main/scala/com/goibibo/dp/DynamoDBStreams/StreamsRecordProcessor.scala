package com.goibibo.dp.DynamoDBStreams

import java.nio.charset.Charset

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.streamsadapter.model.RecordAdapter
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason
import com.amazonaws.services.kinesis.clientlibrary.types.{InitializationInput, ProcessRecordsInput, ShutdownInput}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import com.amazonaws.services.dynamodbv2.document.ItemUtils
import com.amazonaws.services.dynamodbv2.model.AttributeValue

//remove if not needed
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

class StreamsRecordProcessor(dynamoDBClient2: AmazonDynamoDB,
                             private val topicName: String, producer: KafkaProducer[String, String])
  extends IRecordProcessor {

  private var checkpointCounter: java.lang.Integer = _

  private val dynamoDBClient: AmazonDynamoDB = dynamoDBClient2
  override def initialize(initializationInput: InitializationInput): Unit = {
    checkpointCounter = 0
  }

  override def processRecords(processRecordsInput: ProcessRecordsInput): Unit = {
    for (record <- processRecordsInput.getRecords) {
      val data: String =
        new String(record.getData.array(), Charset.forName("UTF-8"))
      println(data)

      if (record.isInstanceOf[RecordAdapter]) {
        val streamRecord: com.amazonaws.services.dynamodbv2.model.Record =
          record.asInstanceOf[RecordAdapter].getInternalObject
        streamRecord.getEventName match {
          case "INSERT" | "MODIFY" =>
            val key = streamRecord.getDynamodb.getKeys
            val keyStr = ItemUtils.toItem(key).toJSON
            val value = streamRecord.getDynamodb.getNewImage
            val valueStr = ItemUtils.toItem(value).toJSON
            println(s"Produce key $keyStr and value $valueStr in Kafka topic ")
            val record = new ProducerRecord(topicName,
              keyStr,
              valueStr)
            producer.send(record)

          case "REMOVE" =>
            val key = streamRecord.getDynamodb.getKeys
            val keyStr = ItemUtils.toItem(key).toJSON
            val valueStr: String = "Deleted"
            println(s"Produce key $key and value $valueStr in Kafka topic ")
            println(s"Produce key $keyStr and value $valueStr in Kafka topic ")
            val record = new ProducerRecord(topicName,
              keyStr,
              valueStr)
            producer.send(record)

        }
      }
      checkpointCounter += 1
      if (checkpointCounter % 10 == 0) {
        try processRecordsInput.getCheckpointer.checkpoint()
        catch {
          case e: Exception => e.printStackTrace()

        }
      }
    }
  }

  override def shutdown(shutdownInput: ShutdownInput): Unit = {
    if (shutdownInput.getShutdownReason == ShutdownReason.TERMINATE) {
      try shutdownInput.getCheckpointer.checkpoint()
      catch {
        case e: Exception => e.printStackTrace()

      }
    }
  }

}