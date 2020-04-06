package com.goibibo.dp.DynamoDBStreams.models

case class KafkaProperties(
                                  bootstrapServers: String,
                                  consumerSessionTimeoutMs: Option[String] = None,
                                  enableAutoCommit: Option[Boolean] = None,
                                  autoCommitIntervalMs: Option[String] = None,
                                  offsetsRetentionMinutes: Option[Long] = None,
                                  autoOffsetReset: Option[String] = None,
                                  fetchMaxBytes: Option[Long] = None,
                                  maxPartitionFetchBytes: Option[Long] = None,
                                  fetchMaxWaitMs: Option[Int] = None,
                                  fetchMinBytes: Option[Int] = None,
                                  compressionType: Option[String] = None,
                                  batchSize: Option[String] = None,
                                  lingerMS: Option[String] = None,
                                  maxPollRecords: Option[Int] = None,
                                  keySerializer: Option[String] = None,
                                  valueSerializer: Option[String] = None
                          ) {
    override def toString: String = {
        s"""
           | Bootstrap Servers: $bootstrapServers
           | consumerSessionTimeoutMs: ${consumerSessionTimeoutMs.orNull}
           | enableAutoCommit: ${enableAutoCommit.orNull}
           | autoCommitIntervalMs: ${autoCommitIntervalMs.orNull}
           | offsetsRetentionMinutes: ${offsetsRetentionMinutes.orNull}
           | autoOffsetReset: ${autoOffsetReset.orNull}
           | fetchMaxBytes: ${fetchMaxBytes.orNull}
           | maxPartitionFetchBytes: ${maxPartitionFetchBytes.orNull}
           | compressionType: ${compressionType.orNull}
           | batchSize: ${batchSize.orNull}
           | lingerMS: ${lingerMS.orNull}
           | fetchMaxWaitMs: ${fetchMaxWaitMs.orNull}
           | fetchMinBytes: ${fetchMinBytes.orNull}
           | maxPollRecords: ${maxPollRecords.orNull}
        """.stripMargin
    }
}