import akka.actor._

import scala.collection.mutable.ArrayBuffer

class Worker extends Actor {

  val neighbors: ArrayBuffer[ActorRef] = new ArrayBuffer()


  def receive = {
    case Connect() => {
      neighbors += sender
    }
  }



}
