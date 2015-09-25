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
      //manager ! new AddedNeighbor()
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

class PushWorker(manager: ActorRef, sInit: Double, wInit: Double ) extends Worker(manager: ActorRef) {

  var s: Double = sInit
  var w: Double = wInit

  var ratio: Double = s/w
  var ratioPrev: Double = s/w

  var termCount: Int = 0

  def receive = {

    case AddNeighbor(neighbor: ActorRef) => {
      neighbors += neighbor
      //println(self.path + " has added " + neighbor.path)
      //manager ! new AddedNeighbor()
    }

    case PushMsg(sMsg: Double, wMsg: Double) => {
      s = s + sMsg
      w = w + wMsg

      ratioPrev = ratio
      ratio = s/w

      if(Math.abs(ratio-ratioPrev) < "10E-10".toDouble) termCount += 1
      else termCount = 0

      if(termCount == 3){
        manager ! new Term()
      }
      else{
        s = s/2
        w = w/2
        neighbors(RNG.getRandNum(neighbors.length)) ! new PushMsg(s,w)
      }

    }

    case Start() => {
      s = s/2
      w = w/2
      neighbors(RNG.getRandNum(neighbors.length)) ! new PushMsg(s,w)
    }

  }

}
