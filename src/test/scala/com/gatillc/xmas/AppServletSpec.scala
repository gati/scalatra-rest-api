package com.gatillc.xmas
import _root_.akka.actor.{ActorSystem, Props}
import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class IndexControllerSpec extends ScalatraSpec { def is =
  "GET / on AppServlet"                     ^
  "should return status 200"                ! root200^
                                            end

  addServlet(new IndexController(ActorSystem()), "/api/*")

  def root200 = get("/api") {
    status must_== 200
  }
}
