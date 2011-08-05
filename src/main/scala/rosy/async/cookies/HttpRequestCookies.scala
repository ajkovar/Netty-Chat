package rosy.async.cookies

import scala.collection.JavaConversions._
import org.jboss.netty.handler.codec.http.CookieDecoder
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.Cookie
import scala.collection.mutable.Set


object HttpRequestCookies {
	implicit def requestCookies(req: HttpRequest) = new {
	  def getCookies(): Map[String, Set[Cookie]] = new CookieDecoder().decode(req.getHeader("Cookie")).groupBy(_.getName)
	  def getCookieValue(name: String): Option[String] = getCookies().get(name) match {
	      case None => None
	      case Some(values) => Some(values.map(_.getValue).first)
	    }
	  }
}