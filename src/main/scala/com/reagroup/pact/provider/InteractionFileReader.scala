package com.reagroup.pact.provider

import java.io.InputStreamReader

import au.com.dius.pact.model.{Interaction, Pact}
import org.springframework.core.io.{DefaultResourceLoader, Resource}
import org.springframework.util.FileCopyUtils

import scala.util.{Failure, Success, Try}

trait InteractionFileReader {

  val testClass: Class[_]

  lazy val allInteractions: Seq[Interaction] = getPactFile(testClass) match {
    case Success(pactFile) => Pact.from(readToString(pactFile)).interactions
    case Failure(e) => throw e
  }

  private def getPactFile(klass: Class[_]): Try[Resource] = {
    Option(klass.getAnnotation(classOf[PactFile]))
      .map(pactFile => Success(new DefaultResourceLoader().getResource(pactFile.value)))
      .getOrElse(Failure(new IllegalStateException("Please use @PactFile to specify the pact file resource")))
  }

  private def readToString(resource: Resource) = {
    FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream, "UTF-8"))
  }

}

