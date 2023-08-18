package func
import scala.util.Random
object func{
/**
 * A function used to generate a random Int List
 * @param min: the min of random number
 * @param max: the max of random number
 * @param len: the length of the List
 * @return the list generated
 */
  def genRandomIntList(min:Int,max:Int,len:Int) : List[Int] = {
    var list = List.empty[Int]
    var random = new Random()
    for (i<-0 until len){
      var randomnum = (min + random.nextFloat()*(max-min)).toInt
      list = list :+ randomnum
    }
    list
  }
}
