import akka.actor.{Props, ActorSystem, ActorRef, Actor}

abstract class Topology(numNodes: Int, man: ActorRef, alg: String) {

  val defaultNumMsgs: Int = 1000
  val topSystem = ActorSystem("TopSystem")
  var numWorkers = numNodes

  var workers: Array[ActorRef]

  def adjustNumWorkers(){
    numWorkers = Math.pow(Math.ceil(Math.pow(numNodes, 1.0/3.0)),3).toInt
    //println(numWorkers)
  }

}

class Line(numNodes: Int, man: ActorRef, alg: String) extends Topology(numNodes: Int, man: ActorRef, alg: String) {

  //Instantiate Worker Array for Line
  var workers = new Array[ActorRef](numWorkers)

  //Instantiate Workers based on Alg Type
  for (i <- workers.indices) {

    if(alg.equals("gossip")) {
      workers(i) = topSystem.actorOf(Props(new GossipWorker(man, defaultNumMsgs)), name = "Worker"+i.toString)
    }
    else{
      workers(i) = topSystem.actorOf(Props(new PushWorker(man, i, 1)), name = "Worker"+i.toString)
    }

  }

  //Create Connections between workers
  for (i <- workers.indices) {

    try {workers(i) ! new AddNeighbor(workers(i-1))}
    catch {case oob: ArrayIndexOutOfBoundsException => {}}

    try {workers(i) ! new AddNeighbor(workers(i+1))}
    catch {case oob: ArrayIndexOutOfBoundsException => {}}

  }

}

class FullNetwork(numNodes: Int, man: ActorRef, alg: String) extends Topology(numNodes: Int, man: ActorRef, alg: String) {

  //Instantiate Worker Array for Line
  var workers = new Array[ActorRef](numWorkers)

  //Instantiate Workers based on Alg Type
  for (i <- workers.indices) {

    if(alg.equals("gossip")) {
      workers(i) = topSystem.actorOf(Props(new GossipWorker(man, defaultNumMsgs)), name = "Worker"+i.toString)
    }
    else{
      workers(i) = topSystem.actorOf(Props(new PushWorker(man, i, 1)), name = "Worker"+i.toString)
    }

  }

  //Create Connections between workers
  for(i <- workers.indices){
    for(j <- workers.indices){
      if(i!=j) workers(i) ! new AddNeighbor(workers(j))
    }
  }

}

class ThreeGrid(numNodes: Int, man: ActorRef, alg: String) extends Topology(numNodes: Int, man: ActorRef, alg: String) {

  adjustNumWorkers()
  val root = Math.round(Math.pow(numWorkers,1.0/3.0)).toInt

  //Instantiate Worker Array for Line
  var workers = new Array[ActorRef](numWorkers)
  var tempArray = Array.ofDim[ActorRef](root,root,root)

  //Instantiate Workers based on Alg Type
  for (i <- workers.indices) {

    if(alg.equals("gossip")) {
      workers(i) = topSystem.actorOf(Props(new GossipWorker(man, defaultNumMsgs)), name = "Worker"+i.toString)
    }
    else{
      workers(i) = topSystem.actorOf(Props(new PushWorker(man, i, 1)), name = "Worker"+i.toString)
    }

  }

  //Create a temporary 3 dimensional array
  var i = 0
  for(x <- tempArray.indices){
    for(y <- tempArray(x).indices){
      for(z <- tempArray(x)(y).indices){
        tempArray(x)(y)(z) = workers(i)
        i+=1
      }
    }
  }

  //Iterate through all of the workers and add the surrounding connections
  for(x <- tempArray.indices){
    for(y <- tempArray(x).indices){
      for(z <- tempArray(x)(y).indices){

        try {tempArray(x)(y)(z) ! new AddNeighbor(tempArray(x+1)(y)(z))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {tempArray(x)(y)(z) ! new AddNeighbor(tempArray(x-1)(y)(z))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {tempArray(x)(y)(z) ! new AddNeighbor(tempArray(x)(y+1)(z))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {tempArray(x)(y)(z) ! new AddNeighbor(tempArray(x)(y-1)(z))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {tempArray(x)(y)(z) ! new AddNeighbor(tempArray(x)(y)(z+1))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {tempArray(x)(y)(z) ! new AddNeighbor(tempArray(x)(y)(z-1))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

      }
    }
  }

}
