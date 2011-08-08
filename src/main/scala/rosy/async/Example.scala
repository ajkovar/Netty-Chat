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
        		client.send("chat-user-join", Map("username" -> data.getValue("username").get, "id" -> data.getValue("id").get))
        	})
        listeners+=groupId->(listeners.getOrElse(groupId, Set.empty)+client)
      })
    })
    
    server.onMessage((client, data) => {
    	println("message")
    	data.getValue("messageType", messageType => {
    	  messageType match {
    	      case "message" => 
    	        data.forEachValueOf("body", body => {
    	          data.forEachValueOf("groupId", groupId => {
    	            data.forEachValueOf("toId", toId => {
    	            	listeners.get(groupId).flatten
		    			.filter(_.data.getValue("id").exists(_!=toId))
		    			.foreach(_.send("chat-message", Map("toId" -> toId, "body" -> body)))
    	            })
    	          })
    	        })
    	      case "list-users" =>
    	        val id = client.data.getValue("id").get
    	        client.data.forEachValueOf("groupId", groupId => {
    	        	client.send("user-list", 
    	        	  Map[String, List[Map[String, Any]]](
	    	        	"users" -> 
		    	          listeners.get(groupId).flatten
		    	        	.filter(_.data.getValue("id").exists(_!=id))
		    	        	.map(listener => {
		    	        	  Map("username" -> listener.data.getValue("username").get,
		    	        	      "id" -> listener.data.getValue("id").get)
		    	        	}).toList
	    	        ))
    	        })
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
