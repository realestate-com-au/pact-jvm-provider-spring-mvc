package com.reagroup.pact.provider

import java.util
import java.util.stream.Collectors

import au.com.dius.pact.model.{Interaction, PactReader}
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.core.io.{DefaultResourceLoader, Resource}

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

trait InteractionFileReader {

  val testClass: Class[_]

  lazy val allInteractions: Seq[Interaction] = getPactFile(testClass) match {
    case Success(pacts) => pacts.flatMap(f => asScalaBuffer(PactReader.loadPact(f.getInputStream).getInteractions).toList)
    case Failure(e) => throw e
  }

  private def getPactFile(klass: Class[_]): Try[List[Resource]] = {
    def readFile(pactFile: PactFile): Resource =
      new DefaultResourceLoader().getResource(pactFile.value)

    def readFiles(pactFolder: PactFolder): List[Resource] =
      util.Arrays
        .stream(ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader()).getResources(pactFolder.value() + "/*.json"))
        .collect(Collectors.toList[Resource])
        .asScala
        .toList


    Option(klass.getAnnotation(classOf[PactFile]))
      .map(pactFile => Success(List(readFile(pactFile))))
      .orElse(Option(klass.getAnnotation(classOf[PactFolder])).map(pactFolder => Success(readFiles(pactFolder))))
      .getOrElse(Failure(new IllegalStateException("Please use @PactFile or @PactFolder to specify the pact file resource")))
  }
}

