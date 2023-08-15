package utils
import chisel3._
import chisel3.util._
class RegisterFile(readPorts:Int,depth:Int,width:Int) extends Module {
  require(readPorts >= 0 && depth >0 && width >0)
  val io = IO(new Bundle{
  val wen   = Input(Bool())
  val waddr = Input(UInt(log2Ceil(depth).W))
  val wdata = Input(UInt(width.W))
  val raddr = Input(Vec(readPorts, UInt(log2Ceil(depth).W)))
  val rdata = Output(Vec(readPorts, UInt(width.W)))
  })
  val reg = RegInit(VecInit(Seq.fill(depth)(0.U(width.W))))
  when (io.wen) {
    reg(io.waddr) := io.wdata
  }
  for (i <- 0 until readPorts) {
      io.rdata(i) := reg(io.raddr(i))
  }
}
