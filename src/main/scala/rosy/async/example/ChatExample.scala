package rosy.async.example
import scala.collection.immutable.Set
import rosy.async.DataStore.requestDataToMap
import rosy.async.Server
import rosy.async.Client

case class ChatClient(id: String, username: String, groups: Set[String])

object ChatExample {
  def main(args : Array[String]) : Unit = {
    
    var groups = Map[String, Set[Client]]()
    var clientData: Map[Client, ChatClient] = Map.empty
    
    val server = new Server
    
    server.onConnect((client, data) => {
      val chatClient = ChatClient(data.getValue("id").get, data.getValue("username").get, data.get("groupId").get)
      
      clientData+=(client->chatClient)
      
      chatClient.groups.foreach((groupId) => {
        groups.get(groupId).flatten
        	.filter(client => { clientData.get(client).get!=chatClient })
        	.foreach(_.send("chat-user-connect", Map("username" -> chatClient.username, "id" -> chatClient.id)))
        	
        groups+=groupId->(groups.getOrElse(groupId, Set.empty)+client)
      })
    })
    
    server.onMessage((client, data) => {
    	val chatClient = clientData.get(client).get
    	data.getValue("messageType", messageType => {
    	  messageType match {
    	      case "message" => 
    	        chatClient.groups.foreach(groupId => {
    	          data.forEachValueOf("body", body => {
    	            data.forEachValueOf("toId", toId => {
    	            	groups.get(groupId).flatten
		    			.filter(otherClient => { clientData.get(otherClient).get.id==toId })
		    			.foreach(_.send("chat-message", Map("toId" -> toId, "body" -> body, "fromId" -> chatClient.id)))
    	            })
    	          })
    	        })
    	      case "list-users" =>
    	        chatClient.groups.foreach(groupId => {
    	        	client.send("user-list", 
    	        	  Map(
	    	        	"users" -> 
		    	          groups.get(groupId).flatten
		    	        	.filter(otherClient => { clientData.get(otherClient).get!=chatClient })
		    	        	// remove duplicates based on "id" value (group them by id and then only take the first)
		    	        	.groupBy(client => { clientData.get(client).get.id }).values.map(_.first)
		    	        	.map(otherClient => {
		    	        	  val otherUser = clientData.get(otherClient).get
		    	        	  Map("username" -> otherUser.username,
		    	        	      "id" -> otherUser.id)
		    	        	}).toList
	    	        ))
    	        })
    	      case _ => println("message type " + messageType + " not found")
    	    }
    	})
    })
    
    server.onDisconnect(client => {
      val chatClient = clientData.get(client).get
      
      chatClient.groups.foreach(groupId => {
        groups+=groupId->(groups.getOrElse(groupId, Set.empty)-client)
        groups.get(groupId).flatten
	    	.filter(otherClient => { clientData.get(otherClient)!=chatClient })
	    	.foreach(otherClient => {
	    		otherClient.send("chat-user-disconnect", Map("id" -> chatClient.id))
	    	})
      })
      println("user " + chatClient.username + " disconnected")
      println("remaining listeners: " + groups)
    })
    
    server.start
  }
}
