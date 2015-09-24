import akka.actor.{Props, ActorSystem, ActorRef, Actor}

abstract class Topology(numWorkers: Int, man: ActorRef, alg: String) {

  val defaultNumMsgs: Int = 1000
  val defaultW: Double = 1.0
  val defaultS: Double = 1.0
  val topSystem = ActorSystem("TopSystem")

  var workers: Array[ActorRef]
}

class Line(numWorkers: Int, man: ActorRef, alg: String) extends Topology(numWorkers: Int, man: ActorRef, alg: String) {

  var workers = new Array[ActorRef](numWorkers)

  for (i <- workers.indices) {

    if(alg.equals("Gossip")) {
      workers(i) = topSystem.actorOf(Props(new GossipWorker(man, defaultNumMsgs)), name = "Worker"+i.toString)
    }
    else{
      //workers(i) = new GossipWorker(man, defaultNumMsgs)
    }

  }

  for (i <- workers.indices) {

    try {
      workers(i) ! new AddNeighbor(workers(i-1))
    }
    catch{
      case oob: ArrayIndexOutOfBoundsException => {}
    }

    try {
      workers(i) ! new AddNeighbor(workers(i+1))
    }
    catch{
      case oob: ArrayIndexOutOfBoundsException => {}
    }

  }

}
