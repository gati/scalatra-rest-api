package com.gatillc.rest_api
import models.Example
import com.mongodb.casbah.Imports._
import akka.actor.ActorSystem

class IndexController(system:ActorSystem, mongoDB:MongoDB)
    extends RestController[Example](system, mongoDB, "test_collection") {
}