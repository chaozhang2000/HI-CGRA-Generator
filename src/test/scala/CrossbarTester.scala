package top 

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Test the Alu design
 */

class CrossbarTester extends AnyFlatSpec with ChiselScalatestTester with Crossbarparams{

  "Crossbar test" should "pass" in {
    test(new Crossbar(inputNum,outputNum,dataWidth)) { dut =>

}
}
}
