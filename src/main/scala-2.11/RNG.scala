import java.util.Random

object RNG {

  val rand = new Random(System.currentTimeMillis())

  def getRandNum(len: Int): Int ={
    return rand.nextInt(len)
  }

}
