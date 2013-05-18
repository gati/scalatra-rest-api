package com.gatillc.rest_api.models

import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._

@Salat
trait Model {
  @Key("_id") def id: ObjectId
  def is_deleted: Boolean
}

case class Example(
  id: ObjectId = new ObjectId, 
  is_deleted: Boolean = false, 
  slug: String, 
  name: String) extends Model

// for optional values name: Option[String]