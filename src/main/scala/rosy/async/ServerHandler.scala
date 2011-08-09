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
import org.joda.time.DateTime
import java.util.Timer
import java.util.TimerTask

class ServerHandler (handler:Handler) extends SimpleChannelUpstreamHandler {
  
  var clients: Map[String, Client] = Map.empty
  
  val timeOutPeriod = 10000L
  
  var timeoutTimer = new Timer
  timeoutTimer.schedule(new TimerTask {
    def run = {
      val maxTimeIdle = (new DateTime).minusMillis(timeOutPeriod.toInt*2)
      val halfLife = (new DateTime).minusMillis(timeOutPeriod.toInt)
      
	  clients.foreach(touple => {
	    val (sessionId, client) = touple
	    
	    // if they are still "connected" but haven't responded in a while, check their pulse
	    if(client.connected && client.lastConnected.compareTo(halfLife)<0) {
	    	client.send("pulse-check", Map())
	    }
	    if(client.lastConnected.compareTo(maxTimeIdle)<0) {
	      println("session "+client.sessionId+" idle for too long. disconnecting.")
	      clients = clients-client.sessionId
	      handler.onDisconnect.apply(client)
	    }
	  })
    }
  }, timeOutPeriod, timeOutPeriod)
  
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
        getSessionId(req) match {
          case None =>
              println("No session")
	    	  val sessionId = UUID.randomUUID.toString
    	      val client = createClient(sessionId, callback, ctx, parameters)
    	      clients+=sessionId->client
    	      client.send("session-established", Map("sessionId" -> sessionId))
    	      handler.onConnect.apply(client, parameters)
          case Some(sessionId) =>
    	      println("Session " + sessionId)
		      val client = clients.get(sessionId) match {
		        case Some(client) =>
		          client.context=ctx
		          client.connected=true
		          client.lastConnected=new DateTime
		        case _ => println("no client found with session ID: " + sessionId)
		      }
        }
      })
    }
    else if(req.getUri.contains("/message")) {
      Util.sendHttpResponse(ctx.getChannel, "")
      getSessionId(req) match {
        case Some(sessionId) =>
          	val client = clients.get(sessionId) match {
		        case Some(client) => 
		          handler.onMessage.apply(client, parameters)
		        case _ => println("no client found with session ID: " + sessionId)
          	}
        case None =>
          	println("message sent with no session")
      }
      
    }
  }
  
  private def getSessionId(req: HttpRequest):Option[String] = {
    if(req.getUri.split("/").length<3) {
      return None
    }
    else {
      return Some(req.getUri.split("/").last.split("\\?").first)
    }
  }
  
  private def createClient(sessionId: String, callback: String, ctx: ChannelHandlerContext, parameters: DataStore): Client =  {
	  val client = new Client(sessionId)
	  client.callback = callback
	  client.context = ctx
	  client.data = parameters
	  client.connected=true
	  client.lastConnected = new DateTime
	  client
  }
}
