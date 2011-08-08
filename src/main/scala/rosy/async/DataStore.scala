package rosy.async

class DataStore(val data: Map[String, Set[String]]) {
  def toMap = data
  def getValue(name: String): Option[String] = {
    if(data.get(name).isDefined)
      return Some(data.get(name).flatten.first)
    else return None
  }
  def getValue(name: String, callback: String => Unit) = {
    if(data.get(name).isDefined) {
      callback(data.get(name).get.first)
    }
  }
  def forEachValueOf(name: String, callback: String => Unit) = {
    data.get(name).flatten.foreach(callback)
  }
}

object DataStore {
  implicit def requestDataToMap(requestData: DataStore) = requestData.data
}