package rosy.async
import scala.collection.immutable.Set
import rosy.async.DataStore.requestDataToMap

object Example {
  def main(args : Array[String]) : Unit = {
    
    var listeners = Map[String, Set[Client]]()
    
    val server = new Server
    
    server.onConnect((client, data) => {
      data.get("groupId").flatten.foreach((groupId) => {
        val id = data.getValue("id").get
        listeners.get(groupId).flatten
        	.filter(_.data.getValue("id").exists(_!=id))
        	.foreach(client => {
        		println("user " + client.data.getValue("username").get)
        		client.send("chat-user-connect", Map("username" -> data.getValue("username").get, "id" -> data.getValue("id").get))
        	})
        var group = listeners.getOrElse(groupId, Set.empty)
        var inGroup = group.exists(_.data.getValue("id").get==id)
        if(!inGroup) {
          listeners+=groupId->(group+client)
        }
      })
    })
    
    server.onMessage((client, data) => {
    	data.getValue("messageType", messageType => {
    	  messageType match {
    	      case "message" => 
    	        val id = client.data.getValue("id").get
    	        client.data.forEachValueOf("groupId", groupId => {
    	          data.forEachValueOf("body", body => {
    	            data.forEachValueOf("toId", toId => {
    	            	listeners.get(groupId).flatten
		    			.filter(_.data.getValue("id").exists(_==toId))
		    			.filter(_.data.getValue("id").exists(_!=id))
		    			.foreach(_.send("chat-message", Map("toId" -> toId, "body" -> body, "fromId" -> id)))
    	            })
    	          })
    	        })
    	      case "list-users" =>
    	        val id = client.data.getValue("id").get
    	        client.data.forEachValueOf("groupId", groupId => {
    	        	client.send("user-list", 
    	        	  Map(
	    	        	"users" -> 
		    	          listeners.get(groupId).flatten
		    	        	.filter(_.data.getValue("id").exists(_!=id))
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
    
    server.onDisconnect((client, data) => {
      data.get("groupId").flatten.foreach((groupId) => {
    	  listeners+=groupId->(listeners.getOrElse(groupId, Set.empty)-client)
      })
    })
    
    server.start
  }
}
