import akka.actor.{Props, ActorSystem, ActorRef, Actor}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

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
  var cubeArray = Array.ofDim[ActorRef](root,root,root)

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
  for(x <- cubeArray.indices){
    for(y <- cubeArray(x).indices){
      for(z <- cubeArray(x)(y).indices){
        cubeArray(x)(y)(z) = workers(i)
        i+=1
      }
    }
  }

  //Iterate through all of the workers and add the surrounding connections
  for(x <- cubeArray.indices){
    for(y <- cubeArray(x).indices){
      for(z <- cubeArray(x)(y).indices){

        try {cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x+1)(y)(z))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x-1)(y)(z))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y+1)(z))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y-1)(z))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y)(z+1))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y)(z-1))}
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

      }
    }
  }

}

class ImpThreeGrid(numNodes: Int, man: ActorRef, alg: String) extends Topology(numNodes: Int, man: ActorRef, alg: String) {

  adjustNumWorkers()
  val root = Math.round(Math.pow(numWorkers,1.0/3.0)).toInt

  //Instantiate Worker Array for Line
  var workers = new Array[ActorRef](numWorkers)
  var cubeArray = Array.ofDim[ActorRef](root,root,root)

  var neighborLists = new Array[mutable.ListBuffer[ActorRef]](numWorkers)
  for(i <- neighborLists.indices){
    neighborLists(i) = new ListBuffer[ActorRef]
  }

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
  for(x <- cubeArray.indices){
    for(y <- cubeArray(x).indices){
      for(z <- cubeArray(x)(y).indices){
        cubeArray(x)(y)(z) = workers(i)
        i+=1
      }
    }
  }

  //Iterate through all of the workers and add the surrounding connections
  i = 0
  for(x <- cubeArray.indices){
    for(y <- cubeArray(x).indices){
      for(z <- cubeArray(x)(y).indices){

        try {
          cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x+1)(y)(z))
          neighborLists(i) += cubeArray(x+1)(y)(z)
        }
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {
          cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x-1)(y)(z))
          neighborLists(i) += cubeArray(x-1)(y)(z)
        }
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {
          cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y+1)(z))
          neighborLists(i) += cubeArray(x)(y+1)(z)
        }
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {
          cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y-1)(z))
          neighborLists(i) += cubeArray(x)(y-1)(z)
        }
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {
          cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y)(z+1))
          neighborLists(i) += cubeArray(x)(y)(z+1)
        }
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        try {
          cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y)(z-1))
          neighborLists(i) += cubeArray(x)(y)(z-1)
        }
        catch {case oob: ArrayIndexOutOfBoundsException => {}}

        i+=1

      }
    }
  }

  for(i <- workers.indices){

    neighborLists(i) += workers(i)

    var randNeighbor = workers(RNG.getRandNum(workers.length))

    while(neighborLists(i).contains(randNeighbor)){
      randNeighbor = workers(RNG.getRandNum(workers.length))
    }

    workers(i) ! new AddNeighbor(randNeighbor)
    neighborLists(i) += randNeighbor

    //println(workers(i).path + " has randomly added " + randNeighbor.path)

  }

}
