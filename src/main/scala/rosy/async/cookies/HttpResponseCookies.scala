package rosy.async.cookies
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.CookieEncoder

object HttpResponseCookies {
	implicit def responseCookies(resp: HttpResponse) = new {
	  def setCookie(name: String, value: String) { 
		  val encoder = new CookieEncoder(true)
		  encoder.addCookie(name, value)
		  resp.setHeader("Set-Cookie", encoder.encode)
	  }
	}
}