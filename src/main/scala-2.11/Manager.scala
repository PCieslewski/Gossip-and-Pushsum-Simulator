import akka.actor.Actor

class Manager() extends Actor{

  //TESTING
  var top: Topology = new Line(100, self, "Gossip")

  var time: Long = System.currentTimeMillis()

  def receive = {

    case Start() => {
      time = System.currentTimeMillis()
      top.workers(RNG.getRandNum(top.workers.length)) ! Rumor()
    }

    case Term() => {
      println(System.currentTimeMillis() - time)
    }

  }

}
