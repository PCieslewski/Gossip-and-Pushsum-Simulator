import akka.actor._

class Manager(numNodes: Int, topString: String, algString: String) extends Actor{

  println("Building " + topString + " Topology...")
  var top: Topology = topFactory()
  var time: Long = System.currentTimeMillis()

  val numWorkers = top.workers.length
  var readyWorkers = 0

  def receive = {

    case Start() => {
      for(i <- top.workers.indices){
        top.workers(i) ! new Ready()
      }
    }

    case StartSimulation() => {
      println("Topology Successfully Created. Starting Simulation...")
      time = System.currentTimeMillis()
      top.workers(RNG.getRandNum(numWorkers)) ! Start()
    }

    case Term() => {
      println("Elapsed Time : " + (System.currentTimeMillis() - time) + " ms.")
      System.exit(0)
    }

    case Ready() =>{
      readyWorkers += 1
      if(readyWorkers == numWorkers){
        self ! new StartSimulation()
      }
    }



  }

  def topFactory(): Topology = topString match{
    case "line" => new Line(numNodes, self, algString)
    case "full" => new FullNetwork(numNodes, self, algString)
    case "3D" => new ThreeGrid(numNodes, self, algString)
    case "imp3D" => new ImpThreeGrid(numNodes, self, algString)
  }

}
