package com.reagroup.pact.provider

import java.net.URI
import java.util.{List => JList}

import org.junit.Assert._
import org.junit.runners.model.{FrameworkMethod, Statement}
import org.mockito.ArgumentMatcher
import org.mockito.Matchers.argThat
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.util.Failure

class PactRunner(klass: Class[_]) extends SpringJUnit4ClassRunner(klass: Class[_]) with InteractionFileReader with InteractionRunner {

  val testClass: Class[_] = klass

  protected override def computeTestMethods: JList[FrameworkMethod] = {
    getTestClass.getAnnotatedMethods(classOf[ProviderState])
  }

  protected override def methodInvoker(method: FrameworkMethod, test: AnyRef): Statement = new Statement() {
    override def evaluate() {
      val controller = method.invokeExplosively(test)
      val providerState = method.getAnnotation(classOf[ProviderState])
      val contextPath = Option(getTestClass.getAnnotation(classOf[ProviderContextPath])).map(_.value())

      findInteractions(providerState.value()) match {
        case Nil => fail("Specified ProviderState is not found: " + providerState)
        case interactions =>
          val timeout = Some(providerState.deferredResponseInMillis()).filter(_ > 0)
          interactions.map(runSingle(_, controller, timeout, contextPath)).foreach {
            case Failure(e) => throw e
            case _ =>
          }
      }
    }
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
