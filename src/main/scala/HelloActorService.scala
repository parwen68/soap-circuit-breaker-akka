import akka.actor.{Actor, Props}
import akka.camel.{CamelMessage, Consumer}
import org.apache.camel.component.cxf.CxfPayload
import org.apache.camel.converter.jaxp.XmlConverter
import scala.util.Random
import scala.xml.{Elem, XML}
import scala.concurrent.duration._

class HelloActor extends Consumer {
  def endpointUri = "cxf:http://localhost:8080/services?wsdlURL=wsdl/hello_world.wsdl&dataFormat=PAYLOAD&loggingFeatureEnabled=true"

  val converter = new XmlConverter

  override def replyTimeout = 30 seconds

  def receive = {
    case CamelMessage(msg: CxfPayload[_],_) => {
      val body = XML.loadString(converter.toString(msg.getBodySources.get(0), null))
      body match {
        case <sayHi/> => context.actorOf(Props[SayHiActor]) forward body
        case <greetMe>{_*}</greetMe> => context.actorOf(Props[GreetMeActor]) forward body
        case <greetMeOneWay>{_*}</greetMeOneWay> => context.actorOf(Props[GreetMeOneWayActor]) forward body
        case <pingMe/> => context.actorOf(Props[PingMeActor]) forward body
      }
    }
  }
}

object R {
  val rand = new Random

  def doReply = rand.nextInt(10) <= 8
}

class SayHiActor extends Actor with Responder {
  def receive = {
    case body: Elem => {
      if(R.doReply) sender ! response(sayHiResponse)
      context.stop(self)
    }
  }

  private def sayHiResponse =
    <sayHiResponse xmlns="http://apache.org/hello_world_soap_http/types">
      <responseType>Response</responseType>
    </sayHiResponse>
}

class GreetMeActor extends Actor with Responder {
  def receive = {
    case body: Elem => {
      val name = body \\ "requestType" text;
      sender ! response(greetMeResponse(name))
      context.stop(self)
    }
  }

  private def greetMeResponse(name: String) =
    <greetMeResponse xmlns="http://apache.org/hello_world_soap_http/types">
      <responseType>{name}</responseType>
    </greetMeResponse>
}

class GreetMeOneWayActor extends Actor with Responder {
  def receive = {
    case body: Elem => println(body \\ "requestType" text); context.stop(self)
  }
}

class PingMeActor extends Actor with Responder {
  def receive = {
    case body: Elem => sender ! response(pingMeResponse); context.stop(self)
  }

  private def pingMeResponse =
      <pingMeResponse xmlns="http://apache.org/hello_world_soap_http/types"/>

}