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

class Client (id: String){
  @BeanProperty var connected = true
  @BeanProperty var data = Map[String, Set[String]]()
  @BeanProperty var callback = ""
  @BeanProperty var context:ChannelHandlerContext = null 
    
  def onDisconnect(callback: ()=>Unit) {}
  
  def send(messageType: String, message :  Any) {
    val messageString = callback + "(" + Json.build(createMessage(messageType, message)).toString + ")"
    println(messageString)
    sendHttpResponse(messageString)
  }
  
  private def createMessage(messageType:String, content: Any) = Map("type" -> messageType, "data" -> content)
  
  private def sendHttpResponse(body: String) {
	val res = new DefaultHttpResponse(HTTP_1_1, OK);
            		
	val content = ChannelBuffers.copiedBuffer(body, CharsetUtil.US_ASCII);
	
    res.setHeader(CONTENT_TYPE, "text/javascript; charset=UTF-8");
    setContentLength(res, content.readableBytes());

    res.setContent(content);    
    
    res.setHeader(CONTENT_TYPE, "text/javascript; charset=UTF-8")
    res.addHeader("Access-Control-Allow-Origin", "*")
    res.addHeader("Access-Control-Allow-Credentials", "true")
    res.addHeader("Connection", "keep-alive")
    setContentLength(res, res.getContent.readableBytes)
    
    val f = context.getChannel.write(res)
    f.addListener(ChannelFutureListener.CLOSE)
  }
}