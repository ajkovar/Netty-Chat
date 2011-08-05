package rosy.async
import scala.reflect.BeanProperty

class Handler {
  @BeanProperty var onConnect: (Client, Map[String, Set[String]])=>Unit = null
  @BeanProperty var onMessage: (Client, Map[String, Set[String]])=>Unit = null
  @BeanProperty var onDisconnect: (Client, Map[String, Set[String]])=>Unit = null
}
				