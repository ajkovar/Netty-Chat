package rosy.async

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
  
  var clients: Map[String, Client] = Map.empty
  
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
	val msg: Object = e.getMessage;
        if (msg.isInstanceOf[HttpRequest]) {  
        	handleHttpRequest(ctx, msg.asInstanceOf[HttpRequest]);
        }
  }
  
  def handleHttpRequest(ctx: ChannelHandlerContext, req: HttpRequest) {
    val parameters = new DataStore(new QueryStringDecoder(req.getUri).getParameters.toMap.map(pair => {
      val (key, value) = pair
      (key, value.toSet)
    }))
    
    if(req.getUri.contains("/connect")) {
      parameters.getValue("callback", (callback) => {
        if(req.getUri.split("/").length<3) {
	    	  println("No session")
	    	  val sessionId = UUID.randomUUID.toString
    	      val client = createClient(sessionId, callback, ctx, parameters)
    	      clients+=sessionId->client
    	      client.send("session-established", Map("sessionId" -> sessionId))
    	      handler.onConnect.apply(client, parameters)
	      }
	      else {
    	      val sessionId = req.getUri.split("/").last.split("\\?").first
		      val client = clients.get(sessionId) match {
    	        case Some(client) =>
    	          client.context=ctx
    	          println("Session " + sessionId)
    	        case _ => println("no client found with session ID: " + sessionId)
    	      }
		      
		  }
      })
    }
    else if(req.getUri.contains("/message")) {
      val sessionId = req.getUri.split("/").last.split("\\?").first
      val client = clients.get(sessionId) match {
        case Some(client) => 
          handler.onMessage.apply(client, parameters)
        case _ => println("no client found with session ID: " + sessionId)
      }
    }
  }
  
  private def createClient(sessionId: String, callback: String, ctx: ChannelHandlerContext, parameters: DataStore): Client =  {
	  val client = new Client(sessionId)
	  client.connected = true
	  client.callback = callback
	  client.context = ctx
	  client.data = parameters
	  client
  }
}
