package com.reagroup.pact.provider

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.util.MultiValueMap
import sample.{MyController, MyService}

class ResponseCheckingTest extends Specification with Mockito {

  isolated

  private val myResponseService = mock[MyService]

  private val myControllerWithService = new MyController().withMyResponseService(myResponseService)

  private val pactRunner = new InteractionFileReader with InteractionRunner {

    @PactFile("file:src/test/resources/interactions.json")
    class TestClass

    override val testClass: Class[_] = classOf[TestClass]
  }

  private val SampleInteraction = pactRunner.findInteractions("normal").head
  private val SampleDeferredInteraction = pactRunner.findInteractions("deferred").head
  private val SampleInteractionWithHeaders = pactRunner.findInteractions("with-headers").head
  private val SampleInteractionWithCookies = pactRunner.findInteractions("with-cookies").head
  private val SampleInteractionWithArrayBody = pactRunner.findInteractions("with-array-body").head

  "non-deferred response checking" should {
    def testWithResponse(response: ResponseEntity[String]) = {
      myResponseService.getResponse[String] returns response
      val result = pactRunner.runSingle(SampleInteraction, myControllerWithService, contextPath = None)
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
    "pass if required request cookies are also matched" in {
      myResponseService.getResponseForCookies[String](Array("value1", "value2")) returns new ResponseEntity[String]( """{ "hello": "world" }""", HttpStatus.OK)
      val result = pactRunner.runSingle(SampleInteractionWithCookies, myControllerWithService, contextPath = None)
      result must beASuccessfulTry
    }
  }

  "deferred response checking" should {
    "pass if response matches expected exactly" in {
      val response = new ResponseEntity[String]( """{ "hello": "world" }""", HttpStatus.OK)
      myResponseService.getResponse[String] returns response
      val result = pactRunner.runSingle(SampleDeferredInteraction, myControllerWithService, timeout = Some(200), contextPath = None)
      result must beASuccessfulTry
    }
  }

  "non-deferred response checking" should {
    "fail if status is not equal" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": "world" }""", HttpStatus.INTERNAL_SERVER_ERROR)
      val result = pactRunner.runSingle(SampleInteraction, myControllerWithService, contextPath = None)
      result must beAFailedTry.which(_.getMessage === "Response status expected:<200> but was:<500>")
    }

    "fail if required header is missing" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": "world" }""", HttpStatus.OK)
      val result = pactRunner.runSingle(SampleInteractionWithHeaders, myControllerWithService, contextPath = None)
      result must beAFailedTry.which(_.getMessage === "Response header my-header1 expected:<my-value1> but was:<null>")
    }

    "fail if header value is not equal" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": "world" }""", createHeaders("my-header1" -> "different-value"), HttpStatus.OK)
      val result = pactRunner.runSingle(SampleInteractionWithHeaders, myControllerWithService, contextPath = None)
      result must beAFailedTry.which(_.getMessage === "Response header my-header1 expected:<my-value1> but was:<different-value>")
    }

    "fail if header value is not equal to expected case sensitively" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": "world" }""", createHeaders("my-header1" -> "MY-VALUE1"), HttpStatus.OK)
      val result = pactRunner.runSingle(SampleInteractionWithHeaders, myControllerWithService, contextPath = None)
      result must beAFailedTry.which(_.getMessage === "Response header my-header1 expected:<my-value1> but was:<MY-VALUE1>")
    }

    "fail if array in body contains unexpected item" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": ["world1", "world2", "unexpected-item-here"]}""", HttpStatus.OK)
      val result = pactRunner.runSingle(SampleInteractionWithArrayBody, myControllerWithService, contextPath = None)
      result must beAFailedTry.which(_.getMessage === "hello[]: Expected 2 values but got 3")
    }
  }

  "deferred response checking" should {
    "fail if status is not equal" in {
      myResponseService.getResponse[String] returns new ResponseEntity[String]( """{ "hello": "world" }""", HttpStatus.INTERNAL_SERVER_ERROR)
      val result = pactRunner.runSingle(SampleDeferredInteraction, myControllerWithService, timeout = Some(200), contextPath = None)
      result must beAFailedTry.which(_.getMessage === "Response status expected:<200> but was:<500>")
    }
  }

  private def createHeaders(keyValues: (String, String)*): MultiValueMap[String, String] = {
    val headers = new HttpHeaders()
    keyValues.foreach {
      case (key, value) => headers.add(key, value)
    }
    headers
  }

}
