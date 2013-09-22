import akka.actor.{Props, ActorSystem}



case class ProxyConf(source: String, target: String, portName: String)

class Proxies(map: List[ProxyConf])(implicit val actorSystem: ActorSystem) {

  def setup = {
    map.map{ case proxyConf =>
      val producer = actorSystem.actorOf(Props(classOf[ProxyOutboundActor], proxyConf))
      val consumer = actorSystem.actorOf(Props(classOf[ProxyInboundActor], proxyConf, producer))
      (consumer, producer)
    }
  }
}

object Main {

  def main(args: Array[String]) = {
    // Create the actor system
    implicit val actorSystem = ActorSystem("ActorSystem")

    val service = actorSystem.actorOf(Props[HelloActor], "service")

    val proxies = new Proxies(List(
      ProxyConf("http://localhost:8081/services", "http://localhost:8080/services","{http://apache.org/hello_world_soap_http}SoapPort"),
      ProxyConf("http://localhost:8081/currency", "http://www.webservicex.net/CurrencyConvertor.asmx", "{http://www.webserviceX.NET/}CurrencyConvertorSoap"),
      ProxyConf("http://localhost:8081/stocks", "http://www.webservicex.net/stockquote.asmx", "{http://www.webserviceX.NET/}StockQuoteSoap")
    )).setup
  }
}