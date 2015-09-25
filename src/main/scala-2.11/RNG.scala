import java.util.Random

object RNG {

  val rand = new Random(System.currentTimeMillis())

  def getRandNum(len: Int): Int ={
    //var a = rand.nextInt(len)
    //println("Input to Rand is: " + len)
    //println("RAND:" + a.toString)
    //return a
    return rand.nextInt(len)
  }

}
