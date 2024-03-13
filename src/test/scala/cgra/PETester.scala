package cgra

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Test the Mem,firstly test the SyncReadMem and then test Mem.
 * the data 0,1,2,3,4,5...is write into Mem by using all writeports parallelly, you can read the code in write method below
 * so the 2^width >=depth is needed
 * then we read data that has been written to memory,and check if they are the right value
 */
class PETester extends AnyFlatSpec with ChiselScalatestTester{
  "PE test" should "pass" in {
    test(new PE(inputCount = 4,outputCount = 4)){ c =>
        for (i <-0 until 107){
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke((i).U)
        c.io.waddr.poke((i).U)
        c.clock.step()
        }
        c.io.wen.poke(false.B)
        c.io.run.poke(true.B)
        println(s"instcnt: ${c.io.rdata(10).peek().litValue()}")
        for (i <-0 until 8){
                println(s"rdata(0): ${c.io.rdata(0).peek().litValue()}")
                println(s"rdata(1): ${c.io.rdata(1).peek().litValue()}")
                println(s"rdata(2): ${c.io.rdata(2).peek().litValue()}")
                println(s"rdata(3): ${c.io.rdata(3).peek().litValue()}")
                println(s"rdata(4): ${c.io.rdata(4).peek().litValue()}")
                println(s"rdata(5): ${c.io.rdata(5).peek().litValue()}")
                println(s"rcdata(0): ${c.io.rdata(6).peek().litValue()}")
                println(s"rcdata(1): ${c.io.rdata(7).peek().litValue()}")
                println(s"rscdata(0): ${c.io.rdata(8).peek().litValue()}")
                println(s"rscdata(1): ${c.io.rdata(9).peek().litValue()}")
                c.clock.step()
        }
    }
  }
}
