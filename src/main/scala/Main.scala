import akka.actor.{Props, ActorSystem}

class Proxies(map: Map[String,String])(implicit val actorSystem: ActorSystem) {

  def setup = {
    map.map{ case (from, to) =>
      val producer = actorSystem.actorOf(Props(classOf[ProxyOutboundActor], to))
      val consumer = actorSystem.actorOf(Props(classOf[ProxyInboundActor], to, from, producer))
      (consumer, producer)
    }
  }
}

object Main {

  def main(args: Array[String]) = {
    // Create the actor system
    implicit val actorSystem = ActorSystem("ActorSystem")

    val service = actorSystem.actorOf(Props[HelloActor], "service")

    val proxies = new Proxies(Map(
      "http://localhost:8081/services" -> "http://localhost:8080/services"
    )).setup
  }
}