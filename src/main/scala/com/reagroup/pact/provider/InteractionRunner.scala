package com.reagroup.pact.provider

import au.com.dius.pact.model.v3.messaging.Message
import au.com.dius.pact.model.{Interaction, RequestResponseInteraction, Response}
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders._
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{request, _}
import org.springframework.test.web.servlet.setup.MockMvcBuilders._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

trait InteractionRunner {
  this: InteractionFileReader =>

  def findInteractions(providerState: String): Seq[Interaction] = {
    allInteractions.filter(_.getProviderState == providerState)
  }

  def runReqResponse(interaction: RequestResponseInteraction, controller: AnyRef, timeout: Option[Long] = None, contextPath: Option[String]): Try[Unit] = {
    RequestMatcherBuilder.build(interaction.getRequest, contextPath).map { requestBuilder =>
      timeout match {
        case Some(t) =>
          val server = standaloneSetup(controller).setAsyncRequestTimeout(t).build()
          val asyncStarted = server.perform(requestBuilder).andExpect(request.asyncStarted).andReturn()
          val response = server.perform(asyncDispatch(asyncStarted))
          responseMatchers(interaction.getResponse).foreach(response.andExpect)
        case None =>
          val server = standaloneSetup(controller).build()
          val response = server.perform(requestBuilder)
          responseMatchers(interaction.getResponse).foreach(response.andExpect)
      }
    }
  }

  def runMsq(interaction: Message, controller: AnyRef, timeout: Option[Long] = None) = ???

  def runSingle(interaction: Interaction, controller: AnyRef, timeout: Option[Long] = None, contextPath: Option[String]): Try[Unit] = {
    interaction match {
      case requestResponse: RequestResponseInteraction =>
        runReqResponse(requestResponse, controller, timeout, contextPath);
      case msg: Message =>
        runMsq(msg, controller, timeout);
    }
  }

  private def responseMatchers(response: Response): Seq[ResultMatcher] = {
    def matchResStatus(response: Response)= status.is(response.getStatus)
    def matchResBodyAsJson(response: Response) = Option(response.getBody.getValue).map(body => content().json(body))
    def matchResHeaders(response: Response) = {
      Option(response.getHeaders) match {
        case None => mutable.Seq.empty
        case Some(h) => for {
          headers: (String, String) <- h.asScala
        } yield header.string(headers._1, headers._2)
      }
    }

    matchResStatus(response) :: matchResBodyAsJson(response).toList ::: matchResHeaders(response).toList
  }

}

