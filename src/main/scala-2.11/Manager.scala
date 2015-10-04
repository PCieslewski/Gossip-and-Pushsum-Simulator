import java.util.concurrent.{TimeUnit, ScheduledFuture}

import akka.actor._
import akka.dispatch.{UnboundedPriorityMailbox, PriorityGenerator}
import com.typesafe.config.Config
import scala.concurrent.duration._

class Manager(numNodes: Int, topString: String, algString: String) extends Actor {

  println("Building " + topString + " Topology with " + numNodes + " " + algString + " workers...")

  //Call the factory function for Topology.
  var top = Topology.factory(self, topString, algString, numNodes)
  var time: Long = System.currentTimeMillis()
  var lastHeartBeat: Long = 0

  val numWorkers = top.workers.length
  var readyWorkers = 0
  var workersThatGotMsgs = 0

  context.system.scheduler.schedule(100 milliseconds, 100 milliseconds, self, CheckHeartbeat())(MySystem.system.dispatcher)

  def receive = {

    //Start by sending a ready check to all actors in the topology
    case Start() => {
      for (i <- top.workers.indices) {
        top.workers(i) ! new Ready()
      }
    }

    case GotMsg() => {
      workersThatGotMsgs += 1
    }

    //Record time and send the start signal to a random actor.
    case StartSimulation() => {
      println("Topology Successfully Created. Starting Simulation...")
      time = System.currentTimeMillis()
      top.workers(RNG.getRandNum(numWorkers)) ! Start()
    }

    //Termination. Record Elapsed Time.
    case Term() => {
      println("Workers that got the msg : " + workersThatGotMsgs)
      println("Elapsed Time : " + (lastHeartBeat - time) + " ms.")
      //System.exit(0)
      sys.exit(0)
      //Main.self ! DoneSystem()
    }

    //When all actors are ready, start timing the simulation
    case Ready() => {
      readyWorkers += 1
      if (readyWorkers == numWorkers) {
        self ! new StartSimulation()
      }
    }

    case Heartbeat() => {
      lastHeartBeat = System.currentTimeMillis()
    }

    case CheckHeartbeat() => {
      if(System.currentTimeMillis() - lastHeartBeat > 100){
        self ! Term()
      }
    }


  }

}

class PrioManMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
  PriorityGenerator {
    case Heartbeat() => 0
    case GotMsg() => 0
    case _ => 1
  })
