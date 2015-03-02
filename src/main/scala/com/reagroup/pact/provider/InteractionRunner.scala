package com.reagroup.pact.provider

import au.com.dius.pact.model.{Interaction, Response}
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultMatchers._
import org.springframework.test.web.servlet.setup.MockMvcBuilders._

import scala.util.Try

trait InteractionRunner {
  this: InteractionFileReader =>

  def findInteractions(pactSetup: ProviderState): Seq[Interaction] = {
    allInteractions.filter(_.providerState.exists(_ == pactSetup.value()))
  }

  def runSingle(interaction: Interaction, controller: AnyRef): Try[Unit] = {
    RequestMatcherBuilder.build(interaction.request).map { request =>
      val response = setupServer(controller).perform(request)
      for (matcher <- responseMatchers(interaction.response)) {
        response.andExpect(matcher)
      }
    }
  }

  private def responseMatchers(response: Response): Seq[ResultMatcher] = {
    def matchResStatus(response: Response) = status.is(response.status)
    def matchResBodyAsJson(response: Response) = response.body.map(body => content.json(body))
    def matchResHeaders(response: Response) = for {
      headers <- response.headers.toList
      (name, value) <- headers
    } yield header.string(name, value)

    matchResStatus(response) :: matchResBodyAsJson(response).toList ::: matchResHeaders(response)
  }

  private def setupServer(controller: Object) = {
    standaloneSetup(controller).build()
  }

}

