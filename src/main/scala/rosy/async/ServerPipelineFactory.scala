package rosy.async

import org.jboss.netty.channel.Channels._
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.handler.codec.http.HttpChunkAggregator
import org.jboss.netty.handler.codec.http.HttpRequestDecoder
import org.jboss.netty.handler.codec.http.HttpResponseEncoder

class ServerPipelineFactory (h:Handler) extends ChannelPipelineFactory {
  var handler: ServerHandler  = new ServerHandler(h)
  
  def getPipeline(): ChannelPipeline = {
    	
        // Create a default pipeline implementation.
        val pl: ChannelPipeline = pipeline()
        pl.addLast("decoder", new HttpRequestDecoder)
        pl.addLast("aggregator", new HttpChunkAggregator(65536))
        pl.addLast("encoder", new HttpResponseEncoder)
        pl.addLast("handler", handler);
        return pl;
    }
}