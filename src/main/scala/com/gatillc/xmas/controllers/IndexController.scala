package com.gatillc.xmas
import models.House
import com.mongodb.casbah.Imports._
import akka.actor.ActorSystem

class IndexController(system:ActorSystem, mongoDB:MongoDB)
    extends RestController[House](system, mongoDB, "test_collection") {
}