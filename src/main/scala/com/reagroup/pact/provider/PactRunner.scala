package com.reagroup.pact.provider

import java.io.InputStreamReader
import java.net.URI
import java.util.{List => JList}

import au.com.dius.pact.model.{Interaction, Pact, Response}
import org.junit.Assert._
import org.junit.runners.model.{FrameworkMethod, Statement}
import org.mockito.ArgumentMatcher
import org.mockito.Matchers.argThat
import org.springframework.core.io.{DefaultResourceLoader, Resource}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, header, status}
import org.springframework.util.FileCopyUtils

import scala.util.{Failure, Success, Try}

class PactRunner(klass: Class[_]) extends SpringJUnit4ClassRunner(klass: Class[_]) {

  private final val allInteractions: Seq[Interaction] = getPactFile(klass) match {
    case Success(pactFile) => Pact.from(readToString(pactFile)).interactions
    case Failure(e) => throw e
  }

  private def getPactFile(klass: Class[_]): Try[Resource] = {
    Option(klass.getAnnotation(classOf[PactFile]))
      .map(pactFile => Success(new DefaultResourceLoader().getResource(pactFile.value)))
      .getOrElse(Failure(new AssertionError("Please use @PactFile to specify the pact file resource")))
  }

  protected override def computeTestMethods: JList[FrameworkMethod] = {
    getTestClass.getAnnotatedMethods(classOf[ProviderState])
  }

  protected override def methodInvoker(method: FrameworkMethod, test: AnyRef): Statement = new Statement() {
    override def evaluate() {
      val controller: AnyRef = method.invokeExplosively(test)
      val pactSetup: ProviderState = method.getAnnotation(classOf[ProviderState])
      pactTestWith(pactSetup, controller)
    }
  }


  private def pactTestWith(pactSetup: ProviderState, controller: AnyRef): Unit = {
    allInteractions.filter(_.providerState.exists(_ == pactSetup.value())) match {
      case Nil => fail("Specified ProviderState is not found: " + pactSetup)
      case interactions => interactions.foreach { interaction =>
        RequestMatcherBuilder.build(interaction.request) match {
          case Success(request) =>
            val response = ServerBuilder.build(controller).perform(request)
            for (matcher <- responseMatchers(interaction.response)) {
              response.andExpect(matcher)
            }
          case Failure(e) => throw e
        }
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

  private def readToString(resource: Resource) = {
    FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream, "UTF-8"))
  }

}

object PactRunner {
  def uriPathEq(expectedPath: String): URI = argThat(new ArgumentMatcher[URI] {
    def matches(argument: AnyRef) = argument match {
      case uri: URI =>
        val actual = uri.getPath + Option(uri.getRawQuery).filterNot(_.isEmpty).map("?" + _).getOrElse("")
        expectedPath == actual
      case _ => false
    }
  })
}
