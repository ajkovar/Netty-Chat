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

class Client (var sessionId: String){
  @BeanProperty var connected = true
  @BeanProperty var data:DataStore = null
  @BeanProperty var callback = ""
  @BeanProperty var context:ChannelHandlerContext = null 
  @BeanProperty var lastConnected:DateTime = null
    
  def onDisconnect(callback: ()=>Unit) {}
  
  def send(messageType: String, message :  Any) {
    val messageString = callback + "(" + Json.build(createMessage(messageType, message)).toString + ")"
    if(context.getChannel.isOpen) {
    	println("Sending message: " + messageString)
    	connected=false
    	Util.sendHttpResponse(context.getChannel, messageString)
    }
    else {
      println("Attempting to send to non connected user message: " + messageString)
    }
  }
  
  private def createMessage(messageType:String, content: Any) = Map("type" -> messageType, "data" -> content)
  
}