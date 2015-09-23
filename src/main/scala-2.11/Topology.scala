import akka.actor.Actor

abstract class Topology(numWorkers: Int, alg: String) {
  def start()
}

class Line(numWorkers: Int, alg: String) extends Topology(numWorkers: Int, alg: String) {
  val workers = new Array[Actor](numWorkers)

  if(alg.equals("Gossip")) {

  }
  else{

  }

}
