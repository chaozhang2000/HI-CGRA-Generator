package utils 


import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

import func._
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

 val testModule = new CrossbarModule(inputNum,outputNum)

 //generate the input and select input and calculate the output
testModule.input = func.genRandomIntList(min = 0,max = 1<<dataWidth-1,len = inputNum) 
testModule.select= func.genRandomIntList(min = 0,max = inputNum,len = outputNum) 
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
