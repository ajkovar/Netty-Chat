package rosy.async

import org.jboss.netty.handler.codec.http.HttpVersion._
import scala.collection.JavaConversions._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import rosy.async.cookies.HttpRequestCookies._
import rosy.async.cookies.HttpResponseCookies._
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.ConcurrentMap
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.MessageEvent
import scala.collection.mutable.Set
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.jboss.netty.handler.codec.http.CookieDecoder
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.Cookie
import org.jboss.netty.handler.codec.http.CookieEncoder
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.HttpResponse
import java.util.UUID
import org.jboss.netty.channel.ChannelFuture
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelFutureListener
import org.jboss.netty.handler.codec.http.QueryStringDecoder

class ServerHandler (handler:Handler) extends SimpleChannelUpstreamHandler {
  
  var clients = new ConcurrentHashMap[String, Client]
  
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
	val msg: Object = e.getMessage;
        if (msg.isInstanceOf[HttpRequest]) {  
        	handleHttpRequest(ctx, msg.asInstanceOf[HttpRequest]);
        }
  }
  
  def handleHttpRequest(ctx: ChannelHandlerContext, req: HttpRequest) {
    println("Request")
    
    val parameters = new QueryStringDecoder(req.getUri()).getParameters().toMap.map((pair) => {
      val (key, value) = pair
      (key, value.toSet)
    })
    
    if(req.getUri().contains("/connect")) {
      parameters.get("callback") match {
	    case Some(values) =>
	      val callback = values.first
	      if(req.getUri().split("/").length<3) {
	    	  println("No session")
	    	  val sessionId = UUID.randomUUID.toString
    	      val client = createClient(sessionId, callback)
    	      client.send("session-established", Map("sessionId" -> sessionId))
    	      handler.onConnect.apply(client, parameters)
	      }
	      else {
	    	  val sessionId = req.getUri().split("/").last
		      println("Session " + sessionId)
		      val client = clients.getOrElse(sessionId, createClient(sessionId, callback))
		      clients.putIfAbsent(sessionId, client)
		  }
      }
    }
    
    def createClient(sessionId: String, callback: String): Client =  {
      val client = new Client(sessionId)
      client.connected = true
      client.callback = callback
      client
    }
    
    def sendHttpResponse(channel: Channel, res: HttpResponse) {
        res.setHeader(CONTENT_TYPE, "text/javascript; charset=UTF-8")
        res.addHeader("Access-Control-Allow-Origin", "*")
        res.addHeader("Access-Control-Allow-Credentials", "true")
        res.addHeader("Connection", "keep-alive")
        setContentLength(res, res.getContent.readableBytes)
        
        val f = channel.write(res)
        f.addListener(ChannelFutureListener.CLOSE)
    }
  }
}
