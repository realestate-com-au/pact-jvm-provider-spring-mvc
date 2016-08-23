package com.reagroup.pact.provider

import java.nio.charset.Charset
import java.util

import au.com.dius.pact.model.{OptionalBody, Request}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.springframework.mock.web.MockServletContext
import org.springframework.util.StreamUtils

import scala.collection.JavaConversions._

class RequestMatcherBuilderTest extends Specification with Mockito {

  isolated

  val builder = RequestMatcherBuilder
  val servletContext = new MockServletContext()

  "request matcher builder" should {

    "build a matcher for http method and path" in {
      val pactRequest = new Request("get", "/hello", null, null, OptionalBody.nullBody(), null)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getMethod === "GET"
        request.getPathInfo === "/hello"
      }
    }

    "build a matcher using provided contextPath" in {
      val pactRequest = new Request("get", "/contextPathProvidedByTheContainer/hello", null, null, OptionalBody.nullBody(), null)
      builder.build(pactRequest, Some("/contextPathProvidedByTheContainer")) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getContextPath === "/contextPathProvidedByTheContainer"
      }
    }

    "build a matcher for query string" in {
      val pactRequest = new Request("get", "/any", mapAsJavaMap(Map("aaa" -> util.Arrays.asList("111"),"bbb"-> util.Arrays.asList("222"))), null, OptionalBody.nullBody(), null)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getQueryString === "aaa=111&bbb=222"
        request.getParameter("aaa") === "111"
        request.getParameter("bbb") === "222"
      }
    }

    "decode query string" in {
      val pactRequest = new Request("get", "/any", mapAsJavaMap(Map("aaa" -> util.Arrays.asList("%22111%22"))),null,OptionalBody.nullBody(),null)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getQueryString === "aaa=%22111%22"
        request.getParameter("aaa") === "\"111\""
      }
    }

    "build a matcher for normal headers (non-cookie)" in {
      val pactRequest = new Request("get", "/any", null, mapAsJavaMap(Map("header1" -> "value1", "header2" -> "value2")), OptionalBody.nullBody(), null)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        request.getHeaderNames.toList === Seq("header1", "header2")
        request.getHeader("header1") === "value1"
        request.getHeaders("header2").toList === Seq("value2")
      }
    }

    "build a matcher for cookie header" in {
      val pactRequest = new Request("get", "/any", null, mapAsJavaMap(Map("Cookie" -> "key1=value1; key2=value2")), OptionalBody.nullBody(), null)
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
      val pactRequest = new Request("get", "/any", null, null, OptionalBody.body("body1"), null)
      builder.build(pactRequest) must beASuccessfulTry.which { builder =>
        val request = builder.buildRequest(servletContext)
        val body = StreamUtils.copyToString(request.getInputStream, Charset.forName("UTF-8"))
        body === "body1"
      }
    }

  }

}
