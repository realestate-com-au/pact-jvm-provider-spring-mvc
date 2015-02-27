package com.reagroup.pact.provider

import org.springframework.test.web.servlet.setup.MockMvcBuilders._

object ServerBuilder {

  def build(controller: Object) = {
    standaloneSetup(controller)
      .setUseTrailingSlashPatternMatch(false)
      .build()
  }

}
