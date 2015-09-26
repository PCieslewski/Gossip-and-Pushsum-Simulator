import java.util.Random

//This file implements a singleton Random Number Generator to generate random indices.
object RNG {

  val rand = new Random(System.currentTimeMillis())

  def getRandNum(len: Int): Int ={
    return rand.nextInt(len)
  }

}
