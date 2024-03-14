package utils
import chisel3._
import chisel3.util._
class Register(width: Int, resetValue: UInt) extends Module {
    val io = IO(new Bundle {
      val inData = Input(UInt(width.W))
      val enable = Input(Bool())
      val outData = Output(UInt(width.W))
    })
      val reg = RegInit(resetValue)
        when(io.enable) {
          reg := io.inData
        }
      io.outData := reg
}
class Regvalid(width: Int, resetValue:UInt,resetvalid:Bool) extends Module {
    val io = IO(new Bundle {
      val inData = Input(Valid(UInt(width.W)))
      val enable = Input(Bool())
      val outData = Output(Valid(UInt(width.W)))
    })
      val reg = RegInit(resetValue)
        when(io.enable) {
          reg := io.inData.bits
        }
      val regvalid = RegInit(resetvalid)
        when(io.enable) {
          regvalid := io.inData.valid
        }
      io.outData.bits := reg
      io.outData.valid:= regvalid
}
