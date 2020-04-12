package com.goibibo.dp.DynamoDBStreams.util

import java.net.InetAddress
import java.util.Properties

import com.goibibo.dp.DynamoDBStreams.models.KafkaProperties
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer}
import org.apache.log4j.Logger

object KafkaUtil {
    private val logger = Logger.getLogger(KafkaUtil.getClass)

    def getConsumerProps(kafkaProperties: KafkaProperties, consumerGroup: String): Properties = {
        val props = new Properties

        logger.info(s"Kafka Properties: ${kafkaProperties.toString}")
        logger.info(s"Kafka consumer group id: $consumerGroup")

        props.put("bootstrap.servers", kafkaProperties.bootstrapServers)
        props.put("group.id", consumerGroup)

        kafkaProperties.autoOffsetReset.foreach { (prop: String) =>
            props.put("auto.offset.reset", prop)
        }

        kafkaProperties.enableAutoCommit.foreach { (prop: Boolean) =>
            props.put("enable.auto.commit", s"$prop")
        }

        kafkaProperties.autoCommitIntervalMs.foreach { (prop: String) =>
            props.put("auto.commit.interval.ms", s"$prop")
        }

        kafkaProperties.consumerSessionTimeoutMs.foreach { (prop: String) =>
            props.put("session.timeout.ms", prop)
        }
        kafkaProperties.fetchMaxBytes.foreach { (prop: Long) =>
            props.put("fetch.max.bytes", s"$prop")
        }
        kafkaProperties.maxPartitionFetchBytes.foreach { (prop: Long) =>
            props.put("max.partition.fetch.bytes", s"$prop")
        }

        kafkaProperties.fetchMaxWaitMs.foreach { (prop: Int) =>
            props.put("fetch.max.wait.ms", s"$prop")
        }

        kafkaProperties.fetchMinBytes.foreach { (prop: Int) =>
            props.put("fetch.min.bytes", s"$prop")
        }

        kafkaProperties.maxPollRecords.foreach { (prop: Int) =>
            props.put("max.poll.records", s"$prop")
        }
        props
    }


    def getProducerProps(kafkaProperties: KafkaProperties): Properties  = {
        val props = new Properties
        props.put("bootstrap.servers", kafkaProperties.bootstrapServers)
        props.put("client.id", InetAddress.getLocalHost.getHostName)
        props.put("acks", "2")
        props.put("key.serializer",kafkaProperties.keySerializer.getOrElse(classOf[StringSerializer].getName))
        props.put("value.serializer", kafkaProperties.valueSerializer.getOrElse(classOf[StringSerializer].getName))
        props.put("compression.type", kafkaProperties.compressionType.get)
        props.put("request.timeout.ms", "100000")
        props
    }
}
