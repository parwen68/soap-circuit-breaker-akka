import akka.camel.CamelMessage
import org.apache.camel.component.cxf.common.message.CxfMessageHelper
import org.apache.camel.component.cxf.CxfPayload
import org.apache.camel.converter.jaxp.XmlConverter
import org.apache.cxf.binding.soap.SoapHeader
import scala.xml.Elem

/**
 *
 */
trait Responder {
  val converter =  new XmlConverter

  import collection.JavaConversions._

  protected def response(body: Elem) = {
    val source = converter.toDOMSource(body.toString())
    new CxfPayload[SoapHeader](null, List(source), null)
  }

  protected def responseMessage(body: Elem) = {
    val source = converter.toDOMSource(body.toString())
    CamelMessage(source, Map())
  }
}