package com.reagroup.pact.provider

import au.com.dius.pact.model.{Interaction, PactReader}
import org.springframework.core.io.{DefaultResourceLoader, Resource}

import scala.collection.JavaConversions.asScalaBuffer
import scala.util.{Failure, Success, Try}

trait InteractionFileReader {

  val testClass: Class[_]

  lazy val allInteractions: Seq[Interaction] = getPactFile(testClass) match {
    case Success(pactFile) => asScalaBuffer(PactReader.loadPact(pactFile.getInputStream).getInteractions).toList
    case Failure(e) => throw e
  }

  private def getPactFile(klass: Class[_]): Try[Resource] = {
    Option(klass.getAnnotation(classOf[PactFile]))
      .map(pactFile => Success(new DefaultResourceLoader().getResource(pactFile.value)))
      .getOrElse(Failure(new IllegalStateException("Please use @PactFile to specify the pact file resource")))
  }
}

