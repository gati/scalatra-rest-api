package com.gatillc.rest_api

import com.mongodb.casbah.Imports._

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.pattern.ask
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, SECONDS}
import akka.util.Timeout

import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

/*
  Base controller class. Each controller is bound to a case class representing 
  a known entity that can be stored in a MongoDB collection and serialized for 
  consumers. The controller instance is passed the process' ActorSytem and 
  MongoDB connection, both instantiated in ScalatraBootstrap. 
 
  The basic GET collection, GET object, POST, PUT and DELETE routes are 
  supported, and child classes can of course implement any additional routes 
  as needed.

  Requests are handled asynchronously using akka.Actors. The RestActor receives 
  a command corresponding to the HTTP route ("create", "update", etc), and 
  whatever objects are required to complete the action - typically a case class 
  instance of ObjectType constructed using JSON from the request body, 
*/

abstract class RestController[ObjectType <: AnyRef]
  (system:ActorSystem, mongoDB: MongoDB, collectionName: String)
  (implicit mot: Manifest[ObjectType])
  extends ScalatraServlet with MethodOverride with FutureSupport with JacksonJsonSupport {

  val mongoCollection = mongoDB(collectionName)
  val actor = system.actorOf(Props(new RestActor[ObjectType](mongoCollection)))

  protected implicit val jsonFormats: Formats = DefaultFormats
  protected implicit def executor: ExecutionContext = system.dispatcher

  implicit val tOut = Timeout(Duration.create(10, SECONDS))

  
  def doSingle(id:String, method:String, modelInstance:Option[ObjectType] = None) = {
    try {
      val objectId = new ObjectId(id.asInstanceOf[String])
      new AsyncResult { def is = actor ? Seq(method, objectId, modelInstance) }
    }
    catch {
      case e: Exception => BadRequest("You probably have a malformed id")
    }
  }

  before() {
    contentType = formats("json")
  }
  
  get("/"){
    new AsyncResult { def is = actor ? "all" }
  }

  get("/:id"){
    val id = params("id")
    doSingle(id, "get")
  }

  post("/") {
    val modelInstance = parsedBody.extract[ObjectType]
    new AsyncResult { def is = actor ? Seq("create", modelInstance) }
  }

  put("/:id") {
    val modelInstance = parsedBody.extract[ObjectType]
    val id = params("id")
    doSingle(id, "update", Some(modelInstance))
  }
  
}
