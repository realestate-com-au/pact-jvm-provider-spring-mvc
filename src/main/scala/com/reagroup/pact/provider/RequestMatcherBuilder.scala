package com.reagroup.pact.provider

import java.net.URLDecoder
import au.com.dius.pact.model.Request
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders._
import org.springframework.web.util.{UriComponents, UriComponentsBuilder}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

object RequestMatcherBuilder {

  def build(request: Request): Try[MockHttpServletRequestBuilder] = {
    val components = buildUriComponents(request)
    createBuildByHttpMethod(request, components).map { builder =>
      buildReqQueryString(builder, components)
      buildReqHeaders(builder, request)
      buildReqBody(builder, request)
      builder
    }
  }

  private def buildUriComponents(request: Request) = {
    UriComponentsBuilder
      .fromUriString(request.path)
      .query(request.query.getOrElse(""))
      .build
  }

  private def createBuildByHttpMethod(request: Request, components: UriComponents): Try[MockHttpServletRequestBuilder] = {
    val path = components.getPath
    request.method.toLowerCase match {
      case "get" => Success(get(path))
      case "post" => Success(post(path))
      case "put" => Success(put(path))
      case "delete" => Success(delete(path))
      case "options" => Success(options(path))
      case "head" => Success(head(path))
      case unknownMethod => Failure(new UnsupportedOperationException(s"Can't handle http method: $unknownMethod"))
    }
  }

  private def buildReqBody(builder: MockHttpServletRequestBuilder, request: Request): Unit = {
    request.body.foreach(body => builder.content(body))
  }

  private def buildReqQueryString(builder: MockHttpServletRequestBuilder, components: UriComponents): Unit = {
    val queryParams = components.getQueryParams
    for (key <- queryParams.keySet) {
      val values = queryParams.get(key).toList
      val decoded = values.map(URLDecoder.decode(_, "UTF-8"))
      builder.param(key, decoded: _*)
    }
  }

  private def buildReqHeaders(builder: MockHttpServletRequestBuilder, request: Request): Unit = for {
    headers <- request.headers
    (name, value) <- headers
  } builder.header(name, value)

}
