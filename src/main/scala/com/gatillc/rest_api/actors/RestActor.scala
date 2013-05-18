package com.gatillc.rest_api
import com.novus.salat._
import com.gatillc.rest_api.context._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import org.scalatra.{NotFound, BadRequest}

/*
  Bound to a case class of type ObjectType, and receives the MongoCollection 
  object that should be used instantiate a data access object (using the 
  com.novus.salat lib), which is in turn used to manage database 
  transactions.

  Inspects the sender's message and returns a serializable object or List.
*/

class RestActor[ObjectType <: AnyRef](mongoCollection:MongoCollection) 
  (implicit mot: Manifest[ObjectType]) extends Actor {
  
  object RestDAO extends SalatDAO[ObjectType, ObjectId](collection = mongoCollection)
  
  // Override this in your child classes if there are other restricted fields
  // in your document. Default is to prevent callers from updating the 
  // document id, which is automatically generated when the case class is
  // instantiated.
  val disallowedFields = List("id", "_id")
  def allowedFieldsList(modelInstance: ObjectType) = {
    modelInstance.getClass.getDeclaredFields.filter(
      field => !disallowedFields.contains(field.getName))
  } 

  def prepareDBObject(modelInstance: ObjectType) = {
    val allowedFields = allowedFieldsList(modelInstance)

    // equiv to allowedFields.foldLeft(newBuilder)((builder,field) => ...)
    val build = (MongoDBObject.newBuilder /: allowedFields) {
      (builder, field) =>
        field.setAccessible(true)
        builder += (field.getName -> field.get(modelInstance))
    }

    build.result
  }

  def doGet(objectId: ObjectId) = {
    sender ! (RestDAO.findOneByID(objectId) match {
      case Some(model:ObjectType) => grater[ObjectType].toJSON(model)
      case _ => NotFound("No doc with that id")
    })
  }

  def doAll() = {
    sender ! grater[ObjectType].toJSONArray(RestDAO.find(MongoDBObject()).toList)
  }

  def doUpdate(objectId: ObjectId, modelInstance: ObjectType) = {
    val cr = RestDAO.update(MongoDBObject("_id" -> objectId), 
      prepareDBObject(modelInstance), false, false)
    sender ! cr
  }

  def doCreate(modelInstance: ObjectType) = {
    sender ! RestDAO.insert(modelInstance)
  }

  def receive = {
    case "all" => doAll()
    case Seq("get", objectId:ObjectId, None) => doGet(objectId)
    case Seq("update", objectId:ObjectId, Some(modelInstance:ObjectType)) => 
      doUpdate(objectId, modelInstance)
    case Seq("create", modelInstance: ObjectType) => doCreate(modelInstance)
  }
}