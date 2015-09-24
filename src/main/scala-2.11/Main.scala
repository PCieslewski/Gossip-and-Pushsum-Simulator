import akka.actor._

object Main {

  def main(args: Array[String]){

    val system = ActorSystem("System")
    val manager = system.actorOf(Props(new Manager()), name = "Manager")

    manager ! Start()

  }

}
