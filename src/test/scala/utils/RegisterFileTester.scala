package utils 

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Test the Binary Mealy state machine design
 */

class RegisterFileTester extends AnyFlatSpec with ChiselScalatestTester{
  "RegisterFile test" should "pass" in {
    val readPorts = 2
    val width =32
    val depth = 32
    test(new RegisterFile(readPorts,depth,width)) { c=>
      def readExpect(addr: Int, value: Int, port: Int = 0): Unit = {
        c.io.raddr(port).poke(addr.U)
        c.io.rdata(port).expect(value.U)
        }
      def write(addr: Int, value: Int): Unit = {
        c.io.wen.poke(true.B)
        c.io.wdata.poke(value.U)
        c.io.waddr.poke(addr.U)
        c.clock.step(1)
        c.io.wen.poke(false.B)
        }
      for (i <- 0 until depth) {
        readExpect(i, 0, port = 0)
        readExpect(i, 0, port = 1)
        }
      for (i <- 0 until depth) {
        write(i, i%width)
        }
      for (i <- 0 until depth) {
        readExpect(i,i%width )
        }
    }
  }
}
