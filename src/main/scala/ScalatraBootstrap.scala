import _root_.akka.actor.{ActorSystem, Props}
import com.mongodb.casbah.Imports._
import com.gatillc.rest_api._
import com.gatillc.rest_api.models._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {

  // Get a handle to an ActorSystem and a reference to one of your actors
  val system = ActorSystem()

  // Setup mongo connection
  val mongoClient =  MongoClient()
  val mongoDB = mongoClient("rest_api")

  // In the init method, mount your servlets with references to the system
  // and/or ActorRefs, as necessary.
  override def init(context: ServletContext) {
    context.mount(new IndexController(system, mongoDB), "/api/*")
  }

  // Make sure you shut down
  override def destroy(context:ServletContext) {
    system.shutdown()
  }
}
