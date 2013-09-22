import akka.actor.{ActorRef, Actor, Status}
import akka.camel.{Producer, CamelMessage, Consumer}
import akka.util.Timeout
import org.apache.camel.converter.stream.InputStreamCache
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import akka.pattern.{CircuitBreaker, ask}

class ProxyInboundActor(pc: ProxyConf, out: ActorRef) extends Consumer {
  def endpointUri = s"cxf:${pc.source}?wsdlURL=${pc.target}%3Fwsdl&dataFormat=RAW&loggingFeatureEnabled=true&portName=${pc.portName}"

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

class ProxyOutboundActor(pc: ProxyConf) extends Actor with Producer {
  def endpointUri = s"cxf:${pc.target}?dataFormat=RAW&portName=${pc.portName}"
}


