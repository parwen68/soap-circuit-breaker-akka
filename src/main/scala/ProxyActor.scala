import akka.actor.{ ActorRef, Actor, Status }
import akka.camel.{Producer, CamelMessage, Consumer}
import akka.util.Timeout
import org.apache.camel.converter.stream.InputStreamCache
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import akka.pattern.{CircuitBreaker, ask}

class ProxyInboundActor(targetUrl: String, publishUrl: String, out: ActorRef) extends Consumer with Responder {
  def endpointUri = s"cxf:${publishUrl}?wsdlURL=${targetUrl}%3Fwsdl&dataFormat=RAW&loggingFeatureEnabled=true"

  import ExecutionContext.Implicits.global

  val breaker =
      new CircuitBreaker(context.system.scheduler,
        maxFailures = 5,
        callTimeout = 10 seconds,
        resetTimeout = 15 seconds)
      .onOpen(notifyMeOnOpen())
      .onHalfOpen(notifyMeOnHalfOpen())
      .onClose(notifyMeOnClose())

  def notifyMeOnOpen() = println(">>> Circuit is now open")
  def notifyMeOnHalfOpen() = println(">>> Circuit is now half open")
  def notifyMeOnClose() = println(">>> Circuit is now closed")

  def receive = {
    case CamelMessage(msg: InputStreamCache,_) => {
      implicit val timeout = Timeout(10 seconds)

      val originalSender = sender
      val future = breaker.withCircuitBreaker(out.ask(msg).mapTo[CamelMessage])

      future.onComplete{
        case Success(reply) => originalSender ! reply
        case Failure(t) => originalSender ! Status.Failure(t)
      }
    }
  }
}

class ProxyOutboundActor(targetUrl: String) extends Actor with Producer {
  def endpointUri = s"cxf:${targetUrl}?dataFormat=RAW"
}


