package com.goibibo.dp

import com.goibibo.dp.models.{TestCase, TestSuite}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._


object TestCases {
    private lazy val configuration: Config = ConfigFactory.load("test-cases").getConfig("Test-Conf")

    def getTestCases: TestSuite = {
        val testCasesAsList: List[Config] = configuration.getConfigList("testCases").asScala.toList
        val testCases = testCasesAsList.map { (testCase: Config) =>

            TestCase()
        }
        TestSuite(testCases)
    }
}
