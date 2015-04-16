package com.reagroup.pact.provider

import java.net.URLDecoder
import javax.servlet.http.Cookie

import au.com.dius.pact.model.Request
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders._
import org.springframework.web.util.{UriComponents, UriComponentsBuilder}

import scala.util.{Failure, Success, Try}

object RequestMatcherBuilder {

  def build(request: Request): Try[MockHttpServletRequestBuilder] = {
    val components = buildUriComponents(request)
    createBuilderByHttpMethod(request, components).map { builder =>
      buildReqHeaders(builder, request)
      buildCookies(builder, request, components)
      buildReqBody(builder, request)
      builder
    }
  }

  private def buildUriComponents(request: Request) = {
    def decode(s: String) = URLDecoder.decode(s, "UTF-8")
    UriComponentsBuilder
      .fromUriString(decode(request.path))
      .query(request.query.map(decode).getOrElse(""))
      .build
  }

  private def createBuilderByHttpMethod(request: Request, components: UriComponents): Try[MockHttpServletRequestBuilder] = {
    val uri = components.toUri
    request.method.toLowerCase match {
      case "get" => Success(get(uri))
      case "post" => Success(post(uri))
      case "put" => Success(put(uri))
      case "delete" => Success(delete(uri))
      case "options" => Success(options(uri))
      case "head" => Success(head(uri))
      case unknownMethod => Failure(new UnsupportedOperationException(s"Can't handle http method: $unknownMethod"))
    }
  }

  private def buildReqBody(builder: MockHttpServletRequestBuilder, request: Request): Unit = {
    request.body.foreach(body => builder.content(body))
  }

  private def buildReqHeaders(builder: MockHttpServletRequestBuilder, request: Request): Unit = for {
    headers <- request.headers
    (name, value) <- headers
  } builder.header(name, value)

  private def buildCookies(builder: MockHttpServletRequestBuilder, request: Request, components: UriComponents): Unit = {
    request.cookie.getOrElse(Nil).map(_.split('=')).collect {
      case Array(key, value) => new Cookie(key, value)
    } match {
      case Nil =>
      case cookies => builder.cookie(cookies: _*)
    }
  }

}
