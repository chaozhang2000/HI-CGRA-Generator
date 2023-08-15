package utils 

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Test the Binary Mealy state machine design
 */

class BinaryMealyTester extends AnyFlatSpec with ChiselScalatestTester{
  val nStates = 3
  val s0 = 2
  def stateTransition(state: Int, in: Boolean): Int = {if (in) 1 else 0}
  def output(state: Int, in: Boolean): Int = {
    if (state == 2) {
        return 0
    }
    if ((state == 1 && !in) || (state == 0 && in)) {
        return 1
    } else {
        return 0
    }
    }

  val testParams = BinaryMealyParams(nStates, s0, stateTransition, output)

  "BinaryMealy test" should "pass" in {
    test(new BinaryMealy(testParams)) { c=>
      c.io.in.poke(false.B)
      c.io.out.expect(0.U)
      c.clock.step(1)
      c.io.in.poke(false.B)
      c.io.out.expect(0.U)
      c.clock.step(1)
      c.io.in.poke(false.B)
      c.io.out.expect(0.U)
      c.clock.step(1)
      c.io.in.poke(true.B)
      c.io.out.expect(1.U)
      c.clock.step(1)
      c.io.in.poke(true.B)
      c.io.out.expect(0.U)
      c.clock.step(1)
      c.io.in.poke(false.B)
      c.io.out.expect(1.U)
      c.clock.step(1)
      c.io.in.poke(true.B)
      c.io.out.expect(1.U)
      c.clock.step(1)
      c.io.in.poke(false.B)
      c.io.out.expect(1.U)
      c.clock.step(1)
      c.io.in.poke(true.B)
      c.io.out.expect(1.U)
}
}
}
