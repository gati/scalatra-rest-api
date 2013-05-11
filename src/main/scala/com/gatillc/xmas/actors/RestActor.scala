package com.gatillc.xmas
import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import org.scalatra.{NotFound, BadRequest}
// models in gatilc.xmas package
import models._

class RestActor[ObjectType <: AnyRef](mongoCollection:MongoCollection) 
  (implicit mot: Manifest[ObjectType]) extends Actor {
  
  object RestDAO extends SalatDAO[ObjectType, ObjectId](collection = mongoCollection)

  def receive = {
    case "all" =>
      sender ! grater[ObjectType].toJSONArray(
        RestDAO.find(MongoDBObject()).toList)
    
    case Seq("get", objectId:ObjectId, None) => 
      sender ! (RestDAO.findOneByID(objectId) match {
        case Some(model:ObjectType) => grater[ObjectType].toJSON(model)
        case _ => NotFound("No doc with that id")
      })

    case Seq("update", objectId:ObjectId, Some(modelInstance:ObjectType)) =>
      sender ! "I would've updated that shit"
    
    case Seq("create", modelInstance: ObjectType) => 
      sender ! RestDAO.insert(modelInstance)

    case Seq("delete", objectId:ObjectId, None) =>
      sender ! "I would've deleted that shit"
  }
}