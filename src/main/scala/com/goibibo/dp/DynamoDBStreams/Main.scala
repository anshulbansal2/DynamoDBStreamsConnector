package com.goibibo.dp.DynamoDBStreams

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.{AmazonCloudWatch, AmazonCloudWatchClientBuilder}
import com.amazonaws.services.dynamodbv2.streamsadapter.{AmazonDynamoDBStreamsAdapterClient, StreamsWorkerFactory}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder, AmazonDynamoDBStreams, AmazonDynamoDBStreamsClientBuilder}
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{InitialPositionInStream, KinesisClientLibConfiguration, Worker}
import com.goibibo.dp.DynamoDBStreams.models.KafkaProperties
import com.goibibo.dp.DynamoDBStreams.util.KafkaUtil
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.log4j.Logger

object Main {

  private val logger: Logger = Logger.getLogger(Main.getClass)

  private var worker: Worker = _
  private var workerConfig: KinesisClientLibConfiguration = _

  private var recordProcessorFactory: IRecordProcessorFactory = _

  private var dynamoDBClient: AmazonDynamoDB = _
  private var cloudWatchClient: AmazonCloudWatch = _
  private var dynamoDBStreamsClient: AmazonDynamoDBStreams = _
  private var adapterClient: AmazonDynamoDBStreamsAdapterClient = _
  var tableName: String = DynamoDBStreamsSettings.DynamoDBTableName
  var streamArn: String = DynamoDBStreamsSettings.streamsARN
  var awsRegion: Regions = Regions.AP_SOUTH_1
  private val awsCredentialsProvider = DefaultAWSCredentialsProviderChain.getInstance

  /**
    * @param args
    */
  def main(args: Array[String]): Unit = {
    logger.info(s"Starting streams for $tableName...")

    val kafkaProperties: KafkaProperties = KafkaProperties(bootstrapServers = DynamoDBStreamsSettings.bootstrapServers,
      compressionType = Some(DynamoDBStreamsSettings.compressionType),
      batchSize = Some(DynamoDBStreamsSettings.batchSize),
      lingerMS = Some(DynamoDBStreamsSettings.lingerMS)
    )

    val producerProps = KafkaUtil.getProducerProps(kafkaProperties)
    val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](producerProps)

    dynamoDBClient = AmazonDynamoDBClientBuilder.standard.withRegion(awsRegion).build
    cloudWatchClient = AmazonCloudWatchClientBuilder.standard.withRegion(awsRegion).build
    dynamoDBStreamsClient = AmazonDynamoDBStreamsClientBuilder.standard.withRegion(awsRegion).build
    adapterClient = new AmazonDynamoDBStreamsAdapterClient(dynamoDBStreamsClient)
    val kafkaTopicName = DynamoDBStreamsSettings.writeTopic

    try {

      recordProcessorFactory = new StreamsRecordProcessorFactory(dynamoDBClient, tableName, kafkaTopicName, producer)

      //streams-adapter-tableName will be created in dynamodb as a leases table to track worker state and dynamodb table

      workerConfig = new KinesisClientLibConfiguration(s"streams-adapter-$tableName",
        streamArn,
        awsCredentialsProvider,
        s"streams-worker-$tableName")
        .withMaxRecords(1000)
        .withIdleTimeBetweenReadsInMillis(500)
        .withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON)

      logger.info("Creating worker for stream: " + streamArn)
      worker = StreamsWorkerFactory.createDynamoDbStreamsWorker(
        recordProcessorFactory,
        workerConfig,
        adapterClient,
        dynamoDBClient,
        cloudWatchClient)

      logger.info("Starting worker...")
      worker.run()
    } catch {
      case e: Exception =>
        logger.error("Error in processing records")
        logger.error(e.getMessage + "\n" + e.getStackTrace.mkString("\n\t"))
    } finally {
      logger.info("Consumer is closed!!!")
      producer.flush()
      producer.close()
      worker.shutdown()
      throw new Exception("Consumer is closed!!!")
    }
  }
}