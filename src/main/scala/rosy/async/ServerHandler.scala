package rosy.async

import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaConcurrentMap
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.immutable.Map

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.jboss.netty.handler.codec.http.HttpRequest
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
    
    val parameters = new QueryStringDecoder(req.getUri).getParameters.toMap.map(pair => {
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
    	      val client = createClient(sessionId, callback, ctx)
    	      client.send("session-established", Map("sessionId" -> sessionId))
    	      handler.onConnect.apply(client, parameters)
	      }
	      else {
    	      val sessionId = req.getUri().split("/").last.split("\\?").first
		      println("Session " + sessionId)
		      val client = clients.getOrElse(sessionId, createClient(sessionId, callback, ctx))
		      clients.putIfAbsent(sessionId, client)
		  }
      }
    }
    
    def createClient(sessionId: String, callback: String, ctx: ChannelHandlerContext): Client =  {
      val client = new Client(sessionId)
      client.connected = true
      client.callback = callback
      client.context = ctx
      client
    }
  }
}
