import akka.actor._

class Manager(numNodes: Int, topString: String, algString: String) extends Actor {

  println("Building " + topString + " Topology...")

  //Call the factory function for Topology.
  var top = Topology.factory(self, topString, algString, numNodes)
  var time: Long = System.currentTimeMillis()

  val numWorkers = top.workers.length
  var readyWorkers = 0

  def receive = {

    //Start by sending a ready check to all actors in the topology
    case Start() => {
      for (i <- top.workers.indices) {
        top.workers(i) ! new Ready()
      }
    }

    //Record time and send the start signal to a random actor.
    case StartSimulation() => {
      println("Topology Successfully Created. Starting Simulation...")
      time = System.currentTimeMillis()
      top.workers(RNG.getRandNum(numWorkers)) ! Start()
    }

    //Termination. Record Elapsed Time.
    case Term() => {
      println("Elapsed Time : " + (System.currentTimeMillis() - time) + " ms.")
      System.exit(0)
    }

    //When all actors are ready, start timing the simulation
    case Ready() => {
      readyWorkers += 1
      if (readyWorkers == numWorkers) {
        self ! new StartSimulation()
      }
    }


  }

}
