package utils 

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Test the Alu design
 * this test is very incomplete, maybe you need to change the range of a,b,op and add some new operations to the case statement to meet your own requirement
 */

class AluTester extends AnyFlatSpec with ChiselScalatestTester with AluTestParam{

  "AluTester test" should "pass" in {
    test(new Alu(dataWidth,functionNum,opneed)) { dut =>

      require(dataWidth>=8&&opneed.length>0)
      // This is exhaustive testing, which usually is not possible
      for (a:Int <- 1 to 15) {
        for (b:Int <- 1 to 15) {
          for (op:Int <- 1 to 4) {
            val result :Int=
              op match {
                case 1 => (a + b)
                case 2 => (a - b)
                case 3 => (a * b)
                case 4 => (a / b)
              }

            dut.io.fn.poke(op.U)
            dut.io.src1.poke(a.S)
            dut.io.src2.poke(b.S)
            dut.io.result.expect(result.S)
          }
        }
      }
    }
  }
}
