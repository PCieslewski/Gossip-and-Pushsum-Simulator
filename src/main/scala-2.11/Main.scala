import akka.actor._

object Main{

  def main(args: Array[String]){

    //val numNodes  = args(0).toInt
    //val topology  = args(1)
    //val algorithm = args(2)

    val numNodes = 3000
    val topology = "imp3D"
    val algorithm = "push-sum"

    val manager = MySystem.system.actorOf(Props(new Manager(numNodes, topology, algorithm)), name = "Manager")
    manager ! new Start()

  }

}

object MySystem {
  val system = ActorSystem("System")
}
