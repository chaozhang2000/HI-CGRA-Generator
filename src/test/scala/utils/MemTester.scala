package utils 

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Test the Mem,firstly test the SyncReadMem and then test Mem.
 * the data 0,1,2,3,4,5...is write into Mem by using all writeports parallelly, you can read the code in write method below
 * so the 2^width >=depth is needed
 * then we read data that has been written to memory,and check if they are the right value
 */
class MemTester extends AnyFlatSpec with ChiselScalatestTester{
  "Mem test" should "pass" in {
    val dwidth =32
    val awidth =32
    val depth =64 
    var syncRead = true
    test(new Memutil(depth = depth,dwidth = dwidth,awidth = awidth, syncRead = syncRead)) { c=>
      def readExpect(addr: Int, value: Int): Unit = {
        c.io.raddr.poke(addr.U)
        c.clock.step(1)
        c.io.rdata.expect(value.U)
        }
      def write(addr: Int, value: Int): Unit = {
        c.io.wen.poke(true.B)
        c.io.wdata.poke(value.U)
        c.io.waddr.poke(addr.U)
        c.clock.step(1)
        c.io.wen.poke(false.B)
        }
      for (i <- 0  until depth) {
        write(i, i)
        }
      for (i <-0  until depth){
        readExpect(i,i)
      }
    }
    syncRead = false
    test(new Memutil(depth = depth,dwidth = dwidth,awidth = awidth, syncRead = syncRead)) { c=>
      def readExpect(addr: Int, value: Int): Unit = {
        c.io.raddr.poke(addr.U)
        c.io.rdata.expect(value.U)
        }
      def write(addr: Int, value: Int): Unit = {
        c.io.wen.poke(true.B)
        c.io.wdata.poke(value.U)
        c.io.waddr.poke(addr.U)
        c.clock.step(1)
        c.io.wen.poke(false.B)
        }
      for (i <- 0  until depth) {
        write(i, i)
        }
      for (i <-0  until depth){
        readExpect(i,i)
      }
    }
  }
}
