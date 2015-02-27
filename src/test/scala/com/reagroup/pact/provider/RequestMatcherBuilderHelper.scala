package com.reagroup.pact.provider

import au.com.dius.pact.model.Request

object RequestMatcherBuilderHelper {

  def requestBuilder(method: String, path: String) = {
    RequestMatcherBuilder.build(newRequest(method, path)).get
  }

  private def newRequest(method: String = "get", path: String, query: Option[String] = None, headers: Option[Map[String, String]] = None,
                         body: Option[String] = None, requestMatchingRules: Option[Map[String, Map[String, String]]] = None) = {
    Request(method, path, query, headers, body, requestMatchingRules)
  }


}
