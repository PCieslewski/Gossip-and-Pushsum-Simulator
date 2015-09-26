import akka.actor._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object Topology {

  //This is the factory function for creating a topology.
  def factory(man: ActorRef, top: String, alg: String, numNodes: Int): Topology = {

    var numWorkers = numNodes

    def adjustNumWorkers() {
      numWorkers = Math.pow(Math.ceil(Math.pow(numNodes, 1.0 / 3.0)), 3).toInt
      println("Number of Workers adjusted to " + numWorkers + ".")
    }

    top match {
      case "line" => {
        new Line(man, alg, numWorkers)
      }
      case "full" => {
        new FullNetwork(man, alg, numWorkers)
      }
      case "3D" => {
        adjustNumWorkers()
        new ThreeGrid(man, alg, numWorkers)
      }
      case "imp3D" => {
        adjustNumWorkers()
        new ImpThreeGrid(man, alg, numWorkers)
      }
    }

  }

  abstract class Topology(man: ActorRef, alg: String, numNodes: Int) {

    val defaultNumMsgs: Int = 1000
    val topSystem = ActorSystem("TopSystem")
    var numWorkers = numNodes

    //Create an array of workers for each topology
    //This automatically calls the factory function for each worker that needs to be created
    var workers: Array[ActorRef] = Array.fill[ActorRef](numNodes)(Worker.factory(man, alg))

  }

  private class Line(man: ActorRef, alg: String, numNodes: Int) extends Topology(man: ActorRef, alg: String, numNodes: Int) {

    //Create Connections between workers
    for (i <- workers.indices) {

      try {
        workers(i) ! new AddNeighbor(workers(i - 1))
      }
      catch {
        case oob: ArrayIndexOutOfBoundsException => {}
      }

      try {
        workers(i) ! new AddNeighbor(workers(i + 1))
      }
      catch {
        case oob: ArrayIndexOutOfBoundsException => {}
      }

    }

  }

  private class FullNetwork(man: ActorRef, alg: String, numNodes: Int) extends Topology(man: ActorRef, alg: String, numNodes: Int) {

    //Create Connections between workers
    for (i <- workers.indices) {
      for (j <- workers.indices) {
        if (i != j) workers(i) ! new AddNeighbor(workers(j))
      }
    }

  }

  private class ThreeGrid(man: ActorRef, alg: String, numNodes: Int) extends Topology(man: ActorRef, alg: String, numNodes: Int) {

    val root = Math.round(Math.pow(numWorkers, 1.0 / 3.0)).toInt

    //Create a 3d array
    var cubeArray = Array.ofDim[ActorRef](root, root, root)

    //Populate the temporary 3 dimensional array
    var i = 0
    for (x <- cubeArray.indices) {
      for (y <- cubeArray(x).indices) {
        for (z <- cubeArray(x)(y).indices) {
          cubeArray(x)(y)(z) = workers(i)
          i += 1
        }
      }
    }

    //Iterate through all of the workers and add the surrounding connections
    for (x <- cubeArray.indices) {
      for (y <- cubeArray(x).indices) {
        for (z <- cubeArray(x)(y).indices) {

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x + 1)(y)(z))
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x - 1)(y)(z))
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y + 1)(z))
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y - 1)(z))
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y)(z + 1))
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y)(z - 1))
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

        }
      }
    }

  }

  private class ImpThreeGrid(man: ActorRef, alg: String, numNodes: Int) extends Topology(man: ActorRef, alg: String, numNodes: Int) {

    val root = Math.round(Math.pow(numWorkers, 1.0 / 3.0)).toInt

    //Create 3d array
    var cubeArray = Array.ofDim[ActorRef](root, root, root)

    //Create a list that keeps track of neighbors added
    var neighborLists = new Array[mutable.ListBuffer[ActorRef]](numWorkers)
    for (i <- neighborLists.indices) {
      neighborLists(i) = new ListBuffer[ActorRef]
    }

    //Create a temporary 3 dimensional array
    var i = 0
    for (x <- cubeArray.indices) {
      for (y <- cubeArray(x).indices) {
        for (z <- cubeArray(x)(y).indices) {
          cubeArray(x)(y)(z) = workers(i)
          i += 1
        }
      }
    }

    //Iterate through all of the workers and add the surrounding connections
    i = 0
    for (x <- cubeArray.indices) {
      for (y <- cubeArray(x).indices) {
        for (z <- cubeArray(x)(y).indices) {

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x + 1)(y)(z))
            neighborLists(i) += cubeArray(x + 1)(y)(z)
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x - 1)(y)(z))
            neighborLists(i) += cubeArray(x - 1)(y)(z)
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y + 1)(z))
            neighborLists(i) += cubeArray(x)(y + 1)(z)
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y - 1)(z))
            neighborLists(i) += cubeArray(x)(y - 1)(z)
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y)(z + 1))
            neighborLists(i) += cubeArray(x)(y)(z + 1)
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          try {
            cubeArray(x)(y)(z) ! new AddNeighbor(cubeArray(x)(y)(z - 1))
            neighborLists(i) += cubeArray(x)(y)(z - 1)
          }
          catch {
            case oob: ArrayIndexOutOfBoundsException => {}
          }

          i += 1

        }
      }
    }

    for (i <- workers.indices) {

      //Add itself to its own neighbor list to ensure that it does not choose itself to be a neighbor
      neighborLists(i) += workers(i)

      var randNeighbor = workers(RNG.getRandNum(workers.length))

      while (neighborLists(i).contains(randNeighbor)) {
        randNeighbor = workers(RNG.getRandNum(workers.length))
      }

      workers(i) ! new AddNeighbor(randNeighbor)
      neighborLists(i) += randNeighbor

      //println(workers(i).path + " has randomly added " + randNeighbor.path)

    }

  }

}