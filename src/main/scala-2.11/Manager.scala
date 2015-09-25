import akka.actor._

class Manager(numNodes: Int, topString: String, algString: String) extends Actor{

  var top: Topology = topFactory()
  var time: Long = System.currentTimeMillis()

  val numWorkers = top.workers.length

  def receive = {

    case Start() => {
      time = System.currentTimeMillis()
      top.workers(RNG.getRandNum(numWorkers)) ! Start()
    }

    case Term() => {
      println(System.currentTimeMillis() - time)
      System.exit(0)
    }

  }

  def topFactory(): Topology = topString match{
    case "line" => new Line(numNodes, self, algString)
    case "full" => new FullNetwork(numNodes, self, algString)
    case "3D" => new ThreeGrid(numNodes, self, algString)
  }

}
