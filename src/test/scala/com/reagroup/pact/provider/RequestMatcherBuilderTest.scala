package com.reagroup.pact.provider

import java.nio.charset.Charset

import au.com.dius.pact.model.Request
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.springframework.mock.web.MockServletContext
import org.springframework.util.StreamUtils

import scala.collection.JavaConversions._

object RequestMatcherBuilderTest extends Specification with Mockito {

  isolated

  val builder = RequestMatcherBuilder
  val servletContext = new MockServletContext()

  "request matcher builder" should {

    "build a matcher for http method and path" in {
      val pactRequest = Request("get", "/hello", None, None, None, None)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getMethod === "GET"
        request.getPathInfo === "/hello"
      }
    }

    "build a matcher for query string" in {
      val pactRequest = Request("get", "/any", Some("aaa=111&bbb=222"), None, None, None)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getQueryString === "aaa=111&bbb=222"
        request.getParameter("aaa") === "111"
        request.getParameter("bbb") === "222"
      }
    }

    "decode query string" in {
      val pactRequest = Request("get", "/any", Some("aaa=%22111%22"), None, None, None)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getQueryString === "aaa=\"111\""
        request.getParameter("aaa") === "\"111\""
      }
    }

    "decode the query string in path" in {
      val pactRequest = Request("get", "/any?aaa=%22111%22", None, None, None, None)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getQueryString === "aaa=\"111\""
        request.getParameter("aaa") === "\"111\""
      }
    }

    "build a matcher for normal headers (non-cookie)" in {
      val pactRequest = Request("get", "/any", None, Some(Map("header1" -> "value1", "header2" -> "value2")), None, None)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getHeaderNames.toList === Seq("header1", "header2")
        request.getHeader("header1") === "value1"
        request.getHeaders("header2").toList === Seq("value2")
      }
    }

    "build a matcher for cookie header" in {
      val pactRequest = Request("get", "/any", None, Some(Map("Cookie" -> "key1=value1; key2=value2")), None, None)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getCookies must have length 2

        val cookie1 = request.getCookies.apply(0)
        cookie1.getName === "key1"
        cookie1.getValue === "value1"

        val cookie2 = request.getCookies.apply(1)
        cookie2.getName === "key2"
        cookie2.getValue === "value2"
      }
    }

    "build a matcher for request body" in {
      val pactRequest = Request("get", "/any", None, None, Some("body1"), None)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        val body = StreamUtils.copyToString(request.getInputStream, Charset.forName("UTF-8"))
        body === "body1"
      }
    }

  }

}
