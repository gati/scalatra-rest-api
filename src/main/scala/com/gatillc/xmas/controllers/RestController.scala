package com.gatillc.xmas
import models._

//import com.mongodb.casbah.{MongoDB, MongoCollection}
import com.mongodb.casbah.Imports._

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.pattern.ask
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, SECONDS}
import akka.util.Timeout

import org.scalatra._

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

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

  delete("/:id") {
    val id = params("id")
    doSingle(id, "delete")
  }
  
}
