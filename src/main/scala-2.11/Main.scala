import akka.actor._

object Main {

  def main(args: Array[String]){

    val numNodes = 64
    val topology = "line"
    val algorithm = "push-sum"

    val system = ActorSystem("System")
    val manager = system.actorOf(Props(new Manager(numNodes, topology, algorithm)), name = "Manager")

    manager ! new Start()

  }

}
