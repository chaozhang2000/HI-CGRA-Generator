package utils
import chisel3._
import chisel3.util._
case class BinaryMealyParams(
    // number of states
    nStates: Int,
    // initial state
    s0: Int,
    // function describing state transition
    stateTransition: (Int, Boolean) => Int,
    // function describing output
    output: (Int, Boolean) => Int
  ) {
    require(nStates >= 0)
    require(s0 < nStates && s0 >= 0)
  }
class BinaryMealy(val mp: BinaryMealyParams) extends Module {
    val io = IO(new Bundle {
      val in = Input(Bool())
      val out = Output(UInt())
    })

    val state = RegInit(UInt(), mp.s0.U)

    // output zero if no states
    io.out := 0.U
    for (i <- 0 until mp.nStates) {
      when (state === i.U) {
        when (io.in) {
          state  := mp.stateTransition(i, true).U
          io.out := mp.output(i, true).U
        }.otherwise {
          state  := mp.stateTransition(i, false).U
          io.out := mp.output(i, false).U
        }
      }
    }
  }
