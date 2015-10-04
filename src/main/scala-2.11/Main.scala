import akka.actor._
import scala.concurrent.duration._

object Main{

  def main(args: Array[String]){

    val numNodes  = args(0).toInt
    val topology  = args(1)
    val algorithm = args(2)

    //val numNodes = 27000
    //val topology = "line"
    //val algorithm = "push-sum"

    val manager = MySystem.system.actorOf(Props(new Manager(numNodes, topology, algorithm)).withDispatcher("prio-dispatcher-man"), name = "Manager")
    manager ! new Start()

  }

}

object MySystem {
  val system = ActorSystem("System")
}
