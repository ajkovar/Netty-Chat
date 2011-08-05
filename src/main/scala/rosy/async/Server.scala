package rosy.async
import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory

class Server {
	var connectCallbacks: Set[(Client, Map[String, Set[String]])=>Unit] = Set.empty
	var messageCallbacks: Set[(Client, Map[String, Set[String]])=>Unit] = Set.empty
	var disconnectCallbacks: Set[(Client, Map[String, Set[String]])=>Unit] = Set.empty
	
	def start {
	  println("Starting server");
		
		// Configure the server.
		var bootstrap:ServerBootstrap = new ServerBootstrap(
		        new NioServerSocketChannelFactory(
		                Executors.newCachedThreadPool(),
		                Executors.newCachedThreadPool()))
		
		val handler = new Handler
		handler.onConnect = (client, data) => {
	    	connectCallbacks.foreach(_.apply(client, data))
	      }
	    handler.onDisconnect = (client, data) => {
	        disconnectCallbacks.foreach(_.apply(client, data))
	      }
	    handler.onMessage = (client, data) => {
	        messageCallbacks.foreach(_.apply(client, data))
	      }
		
		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new ServerPipelineFactory(handler))
		
		// Bind and start to accept incoming connections.
	    bootstrap.bind(new InetSocketAddress(8081))
	}
	def onConnect(callback:(Client, Map[String, Set[String]])=>Unit) = connectCallbacks+=callback
	def onDisconnect(callback:(Client, Map[String, Set[String]])=>Unit) = disconnectCallbacks+=callback
	def onMessage(callback:(Client, Map[String, Set[String]])=>Unit) = messageCallbacks+=callback
}

