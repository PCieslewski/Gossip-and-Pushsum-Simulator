import akka.actor._
import akka.dispatch.{UnboundedPriorityMailbox, PriorityGenerator}
import com.typesafe.config.Config
import scala.collection.mutable.ArrayBuffer

trait Worker extends Actor {
  val neighbors: ArrayBuffer[ActorRef] = new ArrayBuffer()
  var receivedMsg: Boolean = false
  var alive: Boolean = false
}

object Worker {

  //Keep track of how many actors have been created, so each time the factory is called,
  //it can just increment the count by one.
  //val system = ActorSystem("WorkerSystem")
  var workerIndex = -1

  var workersAlive = 0

  //This is the factory method to create workers. It will return either GossipWorker or PushWorker
  //both of which extend the Worker trait
  def factory(manager: ActorRef, algorithm: String): ActorRef = {

    workerIndex += 1

    algorithm match {
      case "gossip" => {
        MySystem.system.actorOf(Props(new GossipWorker(manager)), name = "Worker" + workerIndex.toString)
      }
      case "push-sum" => {
        MySystem.system.actorOf(Props(new PushWorker(manager, workerIndex)).withDispatcher("prio-dispatcher-push"), name = "Worker" + workerIndex.toString)
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

        //If this is the first time hearing a rumor, set status to alive and note that you got a msg.
        if(!receivedMsg){
          receivedMsg = true
          alive = true
          manager ! GotMsg()
        }

        //If youre alive, process the rumor.
        if(alive) {
          manager ! Heartbeat()
          numMsgsTillTerm = numMsgsTillTerm - 1
          if (numMsgsTillTerm == 0) {
            alive = false
          }
          else {
            neighbors(RNG.getRandNum(neighbors.length)) ! new Rumor()
          }
        }

        sender ! Reply()

      }

      case Reply() => {
        if(alive) {
          neighbors(RNG.getRandNum(neighbors.length)) ! new Rumor()
        }
      }

      //Send a rumor to a neighbor!
      case Start() => {
        receivedMsg = true
        alive = true
        manager ! GotMsg()
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

        if(!receivedMsg){
          receivedMsg = true
          alive = true
          workersAlive += 1
          manager ! GotMsg()
        }

        if(alive){

          //println("Normal Process")
          manager ! Heartbeat()
          //println(ratio)

          s = s + sMsg
          w = w + wMsg

          ratioPrev = ratio
          ratio = s / w

          //println(Math.abs(ratio-ratioPrev))

          if (Math.abs(ratio - ratioPrev) < "10E-4".toDouble) termCount += 1
          else termCount = 0

          if (termCount == 3) {
            alive = false
            //manager ! Term()
            workersAlive -= 1
            //println(workersAlive)
            //println("dead")
          }
          else {
            s = s / 2
            w = w / 2
            val n = neighbors(RNG.getRandNum(neighbors.length))
            n ! new PushMsg(s, w)
          }

        }
        else{
          //sender ! PushMsg(sMsg,wMsg)
        }

        //Regardless, always send a reply back.
        sender ! Reply()

      }

      case Start() => {

        receivedMsg = true
        alive = true
        workersAlive += 1
        manager ! GotMsg()

        s = s / 2
        w = w / 2
        val n = neighbors(RNG.getRandNum(neighbors.length))
        n ! new PushMsg(s, w)

      }

      case Ready() => {
        sender ! Ready()
      }

      case Reply() => {

        if(alive) {
          //println("Reply Process")
          s = s / 2
          w = w / 2
          val n = neighbors(RNG.getRandNum(neighbors.length))
          //neighbors(RNG.getRandNum(neighbors.length)) ! new PushMsg(s, w)
          n ! new PushMsg(s, w)
        }
        else{
          //println("test")
        }

      }

    }

  }

}

class PrioPushMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
  PriorityGenerator {
    case AddNeighbor => 0
    case Ready => 1
    case PushMsg(sMsg:Double, wMsg: Double) => 2
    case Reply => 3
    case _ => 4
  })