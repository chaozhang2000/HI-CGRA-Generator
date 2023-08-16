package utils 


import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random
/**
 * Test the Crossbar design
 */

class CrossbarModule(inputNum:Int,outputNum:Int){
  var input = List.fill(inputNum)(0)
  var output = List.empty[Int] 
  var select = List.fill(outputNum)(0)
  def exec(): Unit = {
    for(i<-0 until outputNum){output=output :+ (input(select(i)))}
  }
}
class CrossbarTester extends AnyFlatSpec with ChiselScalatestTester with CrossbarTestParam{
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

 val testModule = new CrossbarModule(inputNum,outputNum)

 //generate the input and select input and calculate the output
testModule.input = genRandomIntList(min = 0,max = 1<<dataWidth-1,len = inputNum) 
testModule.select= genRandomIntList(min = 0,max = inputNum,len = outputNum) 
testModule.exec()
//println(testModule.input)
//println(testModule.select)
//println(testModule.output)

  "Crossbar test" should "pass" in {
    test(new utils.Crossbar(inputNum,outputNum,dataWidth)) { c =>
      for(i<-0 until inputNum){c.io.in(i).poke(testModule.input(i))}
      for(i<-0 until outputNum){c.io.select(i).poke(testModule.select(i))}
      for(i<-0 until outputNum){c.io.out(i).expect(testModule.output(i))}
}
}
}
