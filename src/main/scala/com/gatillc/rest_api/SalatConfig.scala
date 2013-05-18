package com.gatillc.rest_api

import com.novus.salat.{ TypeHintFrequency, StringTypeHintStrategy, Context }
import com.novus.salat.json._

package object context {
  implicit val ctx = new Context {
    val name = "json-test-context"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary,
      typeHint = "_t")
    override val jsonConfig = JSONConfig(
      objectIdStrategy = StringObjectIdStrategy)
  }
}