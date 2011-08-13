package rosy.async
import scala.reflect.BeanProperty
import scala.collection.immutable.Set
import com.twitter.json.Json
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.handler.codec.http.HttpResponse
import scala.collection.immutable.Map
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelFutureListener
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.channel.ChannelHandlerContext
import org.joda.time.DateTime
import org.jboss.netty.channel.ChannelFuture

class Client (var sessionId: String, var context: ChannelHandlerContext, var callback: String){
  @BeanProperty var connected = true
  @BeanProperty var lastConnected:DateTime = null
  var messageQueue: List[Map[String, Any]] = List.empty
    
  def connect = {
    connected=true
    lastConnected = new DateTime
    context.getChannel().getCloseFuture().addListener(new ChannelFutureListener {
      def operationComplete(f: ChannelFuture) = {
        if(f.isSuccess()) {
          connected = false
          println("channel closed")
        }
      }
    })
    if(messageQueue.size>0) {
      sendPayload
    }
  }
  
  def send(messageType: String, message :  Any) {
    messageQueue=messageQueue:+createMessage(messageType, message)
    if(connected) {
    	sendPayload
    }
    else {
      println("Queueing message for non connected user")
    }
  }
  
  private def sendPayload() = {
	var messageString = callback + "(" + Json.build(messageQueue).toString + ")"
	println("Sending message: " + messageString)
	connected=false
	Util.sendHttpResponse(context.getChannel, messageString)
	messageQueue = List.empty
  }
  
  private def createMessage(messageType:String, content: Any) = Map("type" -> messageType, "data" -> content)
  
}