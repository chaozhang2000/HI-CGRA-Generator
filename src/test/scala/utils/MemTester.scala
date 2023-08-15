package utils 

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Test the Binary Mealy state machine design
 */
class MemTester extends AnyFlatSpec with ChiselScalatestTester{
  "Mem test" should "pass" in {
    val readPorts = 2
    val writePorts = 2
    val width =32
    val depth =64 
    var syncRead = true
    test(new Memutil(readPorts,writePorts,depth,width,syncRead)) { c=>
      def readExpect(addr: Int, value: Int, port: Int = 0): Unit = {
        c.io.raddr(port).poke(addr.U)
        c.clock.step(1)
        c.io.rdata(port).expect(value.U)
        }
      def write(addr: Int, value: Int): Unit = {
        for (i <-0 until writePorts){
        c.io.wen(i).poke(true.B)
        c.io.wdata(i).poke((value+i).U)
        c.io.waddr(i).poke((addr+i).U)
        }
        c.clock.step(1)
        for (i <-0 until writePorts){
        c.io.wen(i).poke(false.B)
        }
        }
      for (i <- 0 until depth by writePorts) {
        write(i, i)
        }
      for (i <-0 until (depth/writePorts)*writePorts){
        readExpect(i,i)
      }
    }
    syncRead = false
    test(new Memutil(readPorts,writePorts,depth,width,syncRead)) { c=>
      def readExpect(addr: Int, value: Int, port: Int = 0): Unit = {
        c.io.raddr(port).poke(addr.U)
        c.io.rdata(port).expect(value.U)
        }
      def write(addr: Int, value: Int): Unit = {
        for (i <-0 until writePorts){
        c.io.wen(i).poke(true.B)
        c.io.wdata(i).poke((value+i).U)
        c.io.waddr(i).poke((addr+i).U)
        }
        c.clock.step(1)
        for (i <-0 until writePorts){
        c.io.wen(i).poke(false.B)
        }
        }
      for (i <- 0 until depth by writePorts) {
        write(i, i)
        }
      for (i <-0 until (depth/writePorts)*writePorts){
        readExpect(i,i)
      }
    }
  }
}
