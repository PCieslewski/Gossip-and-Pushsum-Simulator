import akka.actor._

import scala.collection.mutable.ArrayBuffer

abstract class Worker(man: ActorRef) extends Actor{

  val neighbors: ArrayBuffer[ActorRef] = new ArrayBuffer()
  val manager: ActorRef = man

  def addNeighbor(neighbor: ActorRef){
    neighbors += neighbor
  }

}

class GossipWorker(man: ActorRef, numMsgsInit: Int) extends Worker(man: ActorRef) {

  var numMsgsTillTerm = numMsgsInit

  def receive = {

    case Rumor() => {
      numMsgsTillTerm = numMsgsTillTerm - 1
      if(numMsgsTillTerm == 0) {
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

//class PushWorker() extends Worker {
//
//  def receive = {
//    case Connect() => {
//      neighbors += sender
//    }
//  }
//
//}
