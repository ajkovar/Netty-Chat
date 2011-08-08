package rosy.async
import scala.reflect.BeanProperty

class Handler {
  @BeanProperty var onConnect: (Client, DataStore)=>Unit = null
  @BeanProperty var onMessage: (Client, DataStore)=>Unit = null
  @BeanProperty var onDisconnect: (Client, DataStore)=>Unit = null
}
				