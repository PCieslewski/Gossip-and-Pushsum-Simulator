import akka.actor._

import scala.collection.mutable.ArrayBuffer

abstract class Worker(manager: ActorRef) extends Actor{

  val neighbors: ArrayBuffer[ActorRef] = new ArrayBuffer()

}

class GossipWorker(manager: ActorRef, numMsgsInit: Int) extends Worker(manager: ActorRef) {

  var numMsgsTillTerm = numMsgsInit

  def receive = {

    case AddNeighbor(neighbor: ActorRef) => {
      neighbors += neighbor
    }

    case Rumor() => {
      numMsgsTillTerm = numMsgsTillTerm - 1
      if (numMsgsTillTerm == 0) {
        manager ! Term()
      }
      else {
        neighbors(RNG.getRandNum(neighbors.length)) ! new Rumor()
      }
    }

    case Start() => {
      neighbors(RNG.getRandNum(neighbors.length)) ! new Rumor()
    }

  }

}

class PushWorker(manager: ActorRef, sInit: Int, wInit: Int ) extends Worker(manager: ActorRef) {

  var s = sInit
  var w = wInit

  def receive = {

    case AddNeighbor(neighbor: ActorRef) => {
      neighbors += neighbor
    }



  }

}
