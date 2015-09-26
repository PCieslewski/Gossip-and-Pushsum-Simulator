import akka.actor._
import scala.collection.mutable.ArrayBuffer

trait Worker extends Actor {
  val neighbors: ArrayBuffer[ActorRef] = new ArrayBuffer()
}

object Worker {

  //Keep track of how many actors have been created, so each time the factory is called,
  //it can just increment the count by one.
  val workerSystem = ActorSystem("WorkerSystem")
  var workerIndex = -1

  //This is the factory method to create workers. It will return either GossipWorker or PushWorker
  //both of which extend the Worker trait
  def factory(manager: ActorRef, algorithm: String): ActorRef = {

    workerIndex += 1

    algorithm match {
      case "gossip" => {
        workerSystem.actorOf(Props(new GossipWorker(manager)), name = "Worker" + workerIndex.toString)
      }
      case "push-sum" => {
        workerSystem.actorOf(Props(new PushWorker(manager, workerIndex)), name = "Worker" + workerIndex.toString)
      }
    }

  }

  private class GossipWorker(manager: ActorRef) extends Worker {

    //Number of messages till the gossip algorithm has reached termination criteria.
    var numMsgsTillTerm = 10

    def receive = {

      //Add the neighbor that comes in the message
      case AddNeighbor(neighbor: ActorRef) => {
        neighbors += neighbor
      }

      //If you hear a  rumor, decrement the number of messages till termination and resend, otherwise terminate.
      case Rumor() => {
        //println(self.path + " got a rumor!")
        numMsgsTillTerm = numMsgsTillTerm - 1
        if (numMsgsTillTerm == 0) {
          manager ! Term()
        }
        else {
          neighbors(RNG.getRandNum(neighbors.length)) ! new Rumor()
        }
      }

      //Send a rumor to a neighbor!
      case Start() => {
        neighbors(RNG.getRandNum(neighbors.length)) ! new Rumor()
      }

      //Tell the manager you have processed all of your AddNeighbors so you are ready for simulation
      case Ready() => {
        sender ! Ready()
      }

    }

  }

  private class PushWorker(manager: ActorRef, workerIndex: Int) extends Worker {

    var s: Double = workerIndex
    var w: Double = 1

    var ratio: Double = s / w
    var ratioPrev: Double = s / w

    var termCount: Int = 0

    def receive = {

      case AddNeighbor(neighbor: ActorRef) => {
        neighbors += neighbor
      }

      case PushMsg(sMsg: Double, wMsg: Double) => {
        s = s + sMsg
        w = w + wMsg

        ratioPrev = ratio
        ratio = s / w

        if (Math.abs(ratio - ratioPrev) < "10E-10".toDouble) termCount += 1
        else termCount = 0

        if (termCount == 3) {
          manager ! new Term()
        }
        else {
          s = s / 2
          w = w / 2
          neighbors(RNG.getRandNum(neighbors.length)) ! new PushMsg(s, w)
        }

      }

      case Start() => {
        s = s / 2
        w = w / 2
        neighbors(RNG.getRandNum(neighbors.length)) ! new PushMsg(s, w)
      }

      case Ready() => {
        sender ! Ready()
      }

    }

  }

}