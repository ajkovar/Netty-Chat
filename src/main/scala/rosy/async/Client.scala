package rosy.async
import scala.reflect.BeanProperty
import scala.collection.immutable.Set
import com.twitter.json.Json

class Client (id: String){
  @BeanProperty var connected = true
  @BeanProperty var data = Map[String, Set[String]]()
  @BeanProperty var callback = ""
  def onDisconnect(callback: ()=>Unit) {
    
  }
//  def send(message: Map[String, Any]) {
//    println(Json.build(message).toString)
//  }
  def send(messageType: String, message :  Any) {
    println(callback + "(" + Json.build(createMessage(messageType, message)).toString + ")")
  }
  
  private def createMessage(messageType:String, content: Any) = Map("type" -> messageType, "data" -> content)
}