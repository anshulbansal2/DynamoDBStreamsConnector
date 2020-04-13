package com.goibibo.dp.DynamoDBStreams

import java.nio.charset.Charset

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}
import com.amazonaws.services.dynamodbv2.streamsadapter.model.RecordAdapter
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason
import com.amazonaws.services.kinesis.clientlibrary.types.{InitializationInput, ProcessRecordsInput, ShutdownInput}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import com.amazonaws.services.dynamodbv2.document._
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, KeyType, TableDescription}

import scala.util.control.Breaks._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.goibibo.dp.DynamoDBStreams.Main.{awsRegion, dynamoDBClient, logger}

import scala.util.Try

class StreamsRecordProcessor(dynamoDBClient2: AmazonDynamoDB, tableName: String,
                             private val topicName: String, producer: KafkaProducer[String, String])
  extends IRecordProcessor {

  private var checkpointCounter: java.lang.Integer = _

  private val dynamoDBClient: AmazonDynamoDB = dynamoDBClient2
  implicit val table: Table = new DynamoDB(dynamoDBClient).getTable(tableName)

  private val description : TableDescription= table.describe()

  private val hashKey: String = description.getKeySchema.asScala.filter(x=> x.getKeyType.equals(KeyType.HASH.toString)).head.getAttributeName

  private val sortKey  = Try{
    Some(description.getKeySchema.asScala.filter(x=> x.getKeyType.equals(KeyType.RANGE.toString)).head.getAttributeName)
  }.getOrElse(None)

  override def initialize(initializationInput: InitializationInput): Unit = {
    checkpointCounter = 0
  }

  def process(processRecordsInput: ProcessRecordsInput) : Boolean = {
    try {
      for (record <- processRecordsInput.getRecords) {
        val data: String = new String(record.getData.array(), Charset.forName("UTF-8"))

        logger.info(s"DyamoDB Streams Data: $data")

        if (record.isInstanceOf[RecordAdapter]) {
          val streamRecord: com.amazonaws.services.dynamodbv2.model.Record =
            record.asInstanceOf[RecordAdapter].getInternalObject
          streamRecord.getEventName match {

            case "INSERT" | "MODIFY" =>
              val key: java.util.Map[String, AttributeValue] = streamRecord.getDynamodb.getKeys

              val keyStr: String = ItemUtils.toItem(key).toJSON
              logger.info(s"Producing key : $keyStr in kafka")

              val valueStr: String = getItem(key).toJSON
              val record = new ProducerRecord(topicName,
                keyStr,
                valueStr)
                producer.send(record).get()
              logger.info(s"Successfully produced record: $keyStr")

            case "REMOVE" =>
              val key = streamRecord.getDynamodb.getKeys
              val keyStr = ItemUtils.toItem(key).toJSON
              logger.info(s"Deleted record found with key $keyStr")
              val valueStr: String = "Deleted"
              val record = new ProducerRecord(topicName,
                keyStr,
                valueStr)
              producer.send(record).get()
              logger.info(s"Deleted record produced successfully: $keyStr")
          }
        }
        checkpointCounter += 1
        if (checkpointCounter % 10 == 0) {
          logger.info(s"Checkingpointing current progress.. ")
          try processRecordsInput.getCheckpointer.checkpoint()
          catch {
            case e: Exception =>
              logger.error(s"Exception occured during checkpointing: $e")
              e.printStackTrace()
          }
        }
      }
      true
    } catch {
        case e: Exception =>
          logger.error(s"Exception occured in processing records: ${processRecordsInput.getRecords}")
          logger.error(e.getMessage + "\n" + e.getStackTrace.mkString("\n\t"))
          producer.flush()
      false
    }
  }

  override def processRecords(processRecordsInput: ProcessRecordsInput): Unit = {
    val res = process(processRecordsInput)
    if (!res) {
      val retryCounter = 10
      breakable {
        1 to retryCounter foreach { count =>
          logger.info(s"Sleeping for 200 milliseconds, for next retry.")
          Thread.sleep(200)
          logger.info(s"Retrying processRecords because of Failure. Count: ${retryCounter - count}")
          val res = process(processRecordsInput)
          if (res) {
            break
          }
          else if(count == retryCounter) {
            logger.info(s"Records can't be pushed as $retryCounter retries failed")
            logger.info(s"Terminating the process")
            producer.flush()
            System.exit(1)
          }
        }
      }
    }
  }

  def deleteitem(tableName: String): Unit = {
    val dynamoDBClient = AmazonDynamoDBClientBuilder.standard.withRegion(Regions.AP_SOUTH_1).build
    val table: Table = new DynamoDB(dynamoDBClient).getTable(tableName)
    val deleteoutcome = table.scan()
    val iterator = deleteoutcome.iterator

    while ( {
      iterator.hasNext
    }) table.deleteItem("leaseKey", iterator.next.get("leaseKey"))

  }

  def getItem(keys: java.util.Map[String, AttributeValue])(implicit table:Table) : Item = {
      val hashValue: String = keys.asScala(hashKey).getS
      val spec: GetItemSpec = if(sortKey.isDefined) {
        val sortValue: String = keys.asScala(sortKey.get).getS
        new GetItemSpec().withPrimaryKey(hashKey, hashValue, sortKey.get, sortValue)
      }
      else {
        new GetItemSpec().withPrimaryKey(hashKey, hashValue)
      }
      val dynamoItem : Item = table.getItem(spec)
      dynamoItem
    }

  override def shutdown(shutdownInput: ShutdownInput): Unit = {
    if (shutdownInput.getShutdownReason == ShutdownReason.TERMINATE) {
      logger.info(s"Shutdown called, doing Checkpointing before shutdown")
      try shutdownInput.getCheckpointer.checkpoint()
      catch {
        case e: Exception =>
          logger.error(s"Exception occured before shutdown")
          e.printStackTrace()
      }
    }
  }

}