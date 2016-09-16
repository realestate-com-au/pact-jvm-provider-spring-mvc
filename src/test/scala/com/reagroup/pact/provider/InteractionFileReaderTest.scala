package com.reagroup.pact.provider

import java.io.FileNotFoundException

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class InteractionFileReaderTest extends Specification with Mockito {

  "InteractionFileReader" should {
    "read interactions from the JSON file" in {
      val reader = createReader(classOf[TestClass])
      reader.allInteractions must have length 5
      reader.allInteractions.map(_.getProviderState) === Seq("normal", "deferred", "with-headers", "with-cookies", "with-array-body")
    }
    "should report error if file is not found" in {
      val reader = createReader(classOf[TestClassWithFileNotFound])
      reader.allInteractions should throwA[FileNotFoundException]
    }
    "should report @PactFile missing if can't find it" in {
      val reader = createReader(classOf[TestClassWithoutPactFileAnnotation])
      reader.allInteractions should throwA[IllegalStateException]
    }
    "read interactions from different JSON files in a directory" in {
      val reader = createReader(classOf[TestClassWithFolder])
      reader.allInteractions must have length 2
      reader.allInteractions.map(_.getProviderState) === Seq("state 1", "state 2")
    }
  }

  @PactFile("file:src/test/resources/interactions.json")
  class TestClass

  @PactFolder("file:src/test/resources/pacts")
  class TestClassWithFolder

  @PactFile("file:src/test/resources/not-found-file.json")
  class TestClassWithFileNotFound

  class TestClassWithoutPactFileAnnotation

  def createReader(cls: Class[_]) = new InteractionFileReader {
    override lazy val testClass: Class[_] = cls
  }
}
