package com.gatillc.xmas.models

import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._

case class House(@Key("_id") id: ObjectId = new ObjectId, 
  slug: String, name: String)

// for optional values name: Option[String]