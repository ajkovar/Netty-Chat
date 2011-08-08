package rosy.async
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelFutureListener

object Util {
	def sendHttpResponse(channel: Channel, body: String) {
	val res = new DefaultHttpResponse(HTTP_1_1, OK);
            		
	val content = ChannelBuffers.copiedBuffer(body, CharsetUtil.US_ASCII);
	
    res.setHeader(CONTENT_TYPE, "text/javascript; charset=UTF-8");
    setContentLength(res, content.readableBytes);

    res.setContent(content);    
    
    res.setHeader(CONTENT_TYPE, "text/javascript; charset=UTF-8")
    res.addHeader("Access-Control-Allow-Origin", "*")
    res.addHeader("Access-Control-Allow-Credentials", "true")
    res.addHeader("Connection", "keep-alive")
    setContentLength(res, res.getContent.readableBytes)
    
    val f = channel.write(res)
    f.addListener(ChannelFutureListener.CLOSE)
  }
}