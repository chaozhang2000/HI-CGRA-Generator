package cgra
import chisel3._
import chisel3.util._

class Link extends Module with CGRAparams{
    val io = IO(new Bundle {
          val in = Input(Valid(UInt(dwidth.W)))
          val out = Output(UInt(dwidth.W))
    })
      val reg = RegInit(0.U)
        when(io.in.valid) {
          reg := io.in.bits
        }
      io.out:= reg


}
