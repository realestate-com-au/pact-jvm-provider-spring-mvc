package com.reagroup.pact.provider

import au.com.dius.pact.model.{Interaction, Pact}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.util.MultiValueMap
import sample.{MyControllerWithService, MyResponseService}

class ResponseCheckingTest extends Specification with Mockito {

  isolated

  private val myResponseService = mock[MyResponseService]

  private val myControllerWithService = new MyControllerWithService().withMyResponseService(myResponseService)

  private val pactRunner = new InteractionFileReader with InteractionRunner {
    override val testClass: Class[_] = null
    override lazy val allInteractions: Seq[Interaction] = Nil
  }

  private val SampleInteraction = createInteraction(
    """
      |{
      |  "provider_state": "<doesn't matter here>",
      |  "description": "<doesn't matter here>",
      |  "request": {
      |    "method": "get",
      |    "path": "/json",
      |    "headers": {
      |      "Content-Type": "application/json"
      |    }
      |  },
      |  "response": {
      |    "status": 200,
      |    "body": {
      |      "hello": "world"
      |    }
      |  }
      |}
    """.stripMargin)

  private val SampleInteractionWithHeaders = createInteraction(
    """
      |{
      |  "provider_state": "<doesn't matter here>",
      |  "description": "<doesn't matter here>",
      |  "request": {
      |    "method": "get",
      |    "path": "/json",
      |    "headers": {
      |      "Content-Type": "application/json"
      |    }
      |  },
      |  "response": {
      |    "status": 200,
      |    "headers": {
      |      "my-header1": "my-value1"
      |    },
      |    "body": {
      |      "hello": "world"
      |    }
      |  }
      |}
    """.stripMargin)

  private val SampleInteractionWithArrayBody = createInteraction(
    """
      |{
      |  "provider_state": "<doesn't matter here>",
      |  "description": "<doesn't matter here>",
      |  "request": {
      |    "method": "get",
      |    "path": "/json",
      |    "headers": {
      |      "Content-Type": "application/json"
      |    }
      |  },
      |  "response": {
      |    "status": 200,
      |    "body": {
      |      "hello": ["world1", "world2"]
      |    }
      |  }
      |}
    """.stripMargin)

  "response checking" should {
    def testWithResponse(response: ResponseEntity[String]) = {
      myResponseService.getResponse[String] returns response
      val result = pactRunner.runSingle(SampleInteraction, myControllerWithService)
      result must beASuccessfulTry
    }
    "pass if response matches expected exactly" in testWithResponse {
      new ResponseEntity[String]( """{ "hello": "world" }""", HttpStatus.OK)
    }
    "pass even if unexpected response header found" in testWithResponse {
      new ResponseEntity[String]( """{ "hello": "world" }""", createHeaders("extra-header" -> "value"), HttpStatus.OK)
    }
    "pass even if unexpected key in response body found" in testWithResponse {
      new ResponseEntity[String]( """{ "hello": "world", "extra-key": "value" }""", HttpStatus.OK)
    }
  }

  "response checking" should {
    "fail if status is not equal" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": "world" }""", HttpStatus.INTERNAL_SERVER_ERROR)
      val result = pactRunner.runSingle(SampleInteraction, myControllerWithService)
      result must beAFailedTry.which(_.getMessage === "Response status expected:<200> but was:<500>")
    }

    "fail if required header is missing" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": "world" }""", HttpStatus.OK)
      val result = pactRunner.runSingle(SampleInteractionWithHeaders, myControllerWithService)
      result must beAFailedTry.which(_.getMessage === "Response header my-header1 expected:<my-value1> but was:<null>")
    }

    "fail if header value is not equal" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": "world" }""", createHeaders("my-header1" -> "different-value"), HttpStatus.OK)
      val result = pactRunner.runSingle(SampleInteractionWithHeaders, myControllerWithService)
      result must beAFailedTry.which(_.getMessage === "Response header my-header1 expected:<my-value1> but was:<different-value>")
    }

    "fail if header value is not equal to expected case sensitively" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": "world" }""", createHeaders("my-header1" -> "MY-VALUE1"), HttpStatus.OK)
      val result = pactRunner.runSingle(SampleInteractionWithHeaders, myControllerWithService)
      result must beAFailedTry.which(_.getMessage === "Response header my-header1 expected:<my-value1> but was:<MY-VALUE1>")
    }

    "fail if array in body contains unexpected item" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": ["world1", "world2", "unexpected-item-here"]}""", HttpStatus.OK)
      val result = pactRunner.runSingle(SampleInteractionWithArrayBody, myControllerWithService)
      result must beAFailedTry.which(_.getMessage === "hello[]: Expected 2 values but got 3")
    }
  }

  private def createInteraction(json: String): Interaction = {
    Pact.from(
      s"""
         |{
         | "provider": {
         |   "name": "provider-side"
         | },
         | "consumer": {
         |   "name": "consumer-side"
         | },
         | "interactions": [
         |   $json
          | ]
          |}
        """.stripMargin).interactions(0)
  }

  private def createHeaders(keyValues: (String, String)*): MultiValueMap[String, String] = {
    val headers = new HttpHeaders()
    keyValues.foreach {
      case (key, value) => headers.add(key, value)
    }
    headers
  }

}
