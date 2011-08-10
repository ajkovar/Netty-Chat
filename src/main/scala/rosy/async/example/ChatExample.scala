package rosy.async.example
import scala.collection.immutable.Set
import rosy.async.DataStore.requestDataToMap
import rosy.async.Server
import rosy.async.Client

object ChatExample {
  def main(args : Array[String]) : Unit = {
    
    var listeners = Map[String, Set[Client]]()
    
    val server = new Server
    
    server.onConnect((client, data) => {
      data.get("groupId").flatten.foreach((groupId) => {
        val id = data.getValue("id").get
        listeners.get(groupId).flatten
        	.filter(_.data.getValue("id").exists(_!=id))
        	.foreach(client => {
        		client.send("chat-user-connect", Map("username" -> data.getValue("username").get, "id" -> id))
        	})
        listeners+=groupId->(listeners.getOrElse(groupId, Set.empty)+client)
      })
    })
    
    server.onMessage((client, data) => {
    	val id = client.data.getValue("id").get
    	data.getValue("messageType", messageType => {
    	  messageType match {
    	      case "message" => 
    	        client.data.forEachValueOf("groupId", groupId => {
    	          data.forEachValueOf("body", body => {
    	            data.forEachValueOf("toId", toId => {
    	            	listeners.get(groupId).flatten
		    			.filter(_.data.getValue("id").exists(_==toId))
		    			.foreach(_.send("chat-message", Map("toId" -> toId, "body" -> body, "fromId" -> id)))
    	            })
    	          })
    	        })
    	      case "list-users" =>
    	        client.data.forEachValueOf("groupId", groupId => {
    	        	client.send("user-list", 
    	        	  Map(
	    	        	"users" -> 
		    	          listeners.get(groupId).flatten
		    	        	.filter(_.data.getValue("id").exists(_!=id))
		    	        	// remove duplicates based on "id" value (group them by id and then only take the first)
		    	        	.groupBy(_.data.getValue("id").get).values.map(_.first)
		    	        	.map(listener => {
		    	        	  Map("username" -> listener.data.getValue("username").get,
		    	        	      "id" -> listener.data.getValue("id").get)
		    	        	}).toList
	    	        ))
    	        })
    	      case _ => println("message type " + messageType + " not found")
    	    }
    	})
    })
    
    server.onDisconnect(client => {
      client.data.forEachValueOf("groupId", groupId => {
    	  listeners+=groupId->(listeners.getOrElse(groupId, Set.empty)-client)
      })
      
      val id = client.data.getValue("id").get
      client.data.forEachValueOf("groupId", groupId=>{
        listeners.get(groupId).flatten
	    	.filter(_.data.getValue("id").exists(_!=id))
	    	.foreach(client => {
	    		client.send("chat-user-disconnect", Map("id" -> id))
	    	})
      })
      println("user " + client.data.getValue("username").get + " disconnected")
      println("remaining listeners: " + listeners)
    })
    
    server.start
  }
}
