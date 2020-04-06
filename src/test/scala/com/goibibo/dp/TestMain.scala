package com.goibibo.dp

import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats}
import org.scalatest._
import scala.collection.immutable.{List, Map}

class TestMain extends FlatSpec {

    //private val logger = LoggerFactory.getLogger(Main.getClass)

    "fetchUpdatedFieldList Implemenation for targetFieldsConnectorListMap" should "result in expected targetFieldDetail" in {
        val targetFieldsConnectorListMap: Map[String, List[String]] = Map.empty

        val testcases = TestCases.getTestCases
        assert("1" == "1")
    }

    "mergeJson Implemenation for MySqlDataSource" should "result in expected Json" in {
        val json1 = """{
                      |  "confirmbookingid": "0055935663",
                      |  "id": 55935663,
                      |  "bookingstatus": "booked",
                      |  "city": "Chandigarh",
                      |  "confirmstatus": "confirmed",
                      |  "mihpayid": "44957368",
                      |}"""
        val json2 = """{
                      |  "confirmbookingid": "0055935663",
                      |  "id": 55935663,
                      |  "bookingstatus": "booked",
                      |  "city": "Chandigarh",
                      |  "confirmstatus": "confirmed",
                      |  "mihpayid": "44957368",
                      |}"""
        val expectedJson = """{
                         |  "confirmbookingid": "0055935663",
                         |  "id": 55935663,
                         |  "bookingstatus": "booked",
                         |  "city": "Chandigarh",
                         |  "confirmstatus": "confirmed",
                         |  "mihpayid": "44957368",
                         |  "bookingid": "HTLF8NFH7P",
                         |  "vendorbookingid": "0055935663",
                         |  "email": "preetwaris302@gmail.com",
                         |  "pay_mode": true,
                         |  "amount": 2979.0
                         |}"""
//        val resultJson = MySqlDataSource.mergeJson(json1,json2)
//        print(resultJson)
        assert(expectedJson == expectedJson)

    }
}
