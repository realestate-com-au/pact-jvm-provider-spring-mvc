package com.reagroup.pact.provider

import au.com.dius.pact.model.Request
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import java.util.{Map => JMap}

import scala.collection.JavaConversions._

object RequestBuilder {

  private var method: String = "get"
  private var path: Option[String] = None
  private var query: Option[String] = None
  private var headers: Option[Map[String, String]] = None
  private var body: Option[String] = None
  private var requestMatchingRules: Option[Map[String, Map[String, String]]] = None

  def method(value: String): this.type = {
    this.method = value
    this
  }

  def path(value: String): this.type = {
    this.path = Some(value)
    this
  }

  def query(value: String): this.type = {
    this.query = Some(value)
    this
  }

  def headers(value: JMap[String, String]): this.type = {
    this.headers = Some(value.toMap)
    this
  }

  def body(value: String): this.type = {
    this.body = Some(value)
    this
  }

  def requestMatchingRules(value: JMap[String, JMap[String, String]]): this.type = {
    this.requestMatchingRules = Some(value.map(kv => (kv._1, kv._2.toMap)).toMap)
    this
  }

  def build(): MockHttpServletRequestBuilder = {
    val request = Request(method, path.getOrElse("/"), query, headers, body, requestMatchingRules)
    RequestMatcherBuilder.build(request).get
  }

}
