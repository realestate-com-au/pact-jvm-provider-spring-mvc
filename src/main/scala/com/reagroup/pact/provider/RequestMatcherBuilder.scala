package com.reagroup.pact.provider

import java.net.URLDecoder
import javax.servlet.http.Cookie

import au.com.dius.pact.model.Request
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders._
import org.springframework.web.util.{UriComponents, UriComponentsBuilder}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object RequestMatcherBuilder {

  def build(request: Request, contextPath: Option[String] = None): Try[MockHttpServletRequestBuilder] = {
    val builder = createBuilder(request, buildUriComponents(request))
    contextPath match {
      case Some(path) => builder.map(builder => builder.contextPath(path))
      case None => builder
    }
  }

  private def createBuilder(request: Request, components: UriComponents): Try[MockHttpServletRequestBuilder] = {
    createBuilderByHttpMethod(request, components).map { builder =>
      buildReqHeaders(builder, request)
      buildCookies(builder, request, components)
      buildReqBody(builder, request)
      builder
    }
  }

  private def buildUriComponents(request: Request) = {
    def decode(s: String) = {
      Option(s) match {
        case Some(q) => URLDecoder.decode(q, "UTF-8")
        case None => null
      }
    }

    def toQuery(map: java.util.Map[String, java.util.List[String]]): String = {
      Option(map) match {
        case Some(q) => (for {
          m <- q.asScala
        } yield m._1 + "=" + m._2.asScala.map(URLDecoder.decode(_, "UTF-8")).mkString(";")).mkString("&")
        case None => null
      }
    }

    UriComponentsBuilder
      .fromUriString(decode(request.getPath))
      .query(decode(toQuery(request.getQuery)))
      .build
  }

  private def createBuilderByHttpMethod(request: Request, components: UriComponents): Try[MockHttpServletRequestBuilder] = {
    val uri = components.toUri
    request.getMethod.toLowerCase match {
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
    Option(request.getBody) match {
      case Some(b) => builder.content(b.orElse(""))
      case None =>
    }
  }

  private def buildReqHeaders(builder: MockHttpServletRequestBuilder, request: Request): Unit = {
    Option(request.getHeaders) match {
      case Some(h) => for {
        headers: (String, String) <- h.asScala
      } builder.header(headers._1, headers._2)
      case None =>
    }
  }

  private def buildCookies(builder: MockHttpServletRequestBuilder, request: Request, components: UriComponents): Unit = {
    Option(request.cookie()) match {
      case Some(c) => c.asScala.map(_.split('=')).collect {
        case Array(key, value) => new Cookie(key, value)
      } match {
        case cookies => builder.cookie(cookies: _*)
      }
      case None =>
    }
  }

}
