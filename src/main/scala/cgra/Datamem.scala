package cgra
import chisel3._
import chisel3.util._
class DataMemIO extends Bundle with CGRAparams{
  val wen = Input(Bool())
  val waddr = Input(UInt(dataMemaWidth.W))
  val wdata = Input(UInt(dataMemdWidth.W))
  val ren = Input(Bool())
  val raddr = Input(UInt(dataMemaWidth.W))
  val rdata = Output(UInt(dataMemdWidth.W))
  val memoptvalid = Output(Bool())
}

class Datamem extends Module with CGRAparams{
  val io = IO(new DataMemIO)
  val mem = Module(new utils.Memutil(depth=dataMemSize,dwidth=dataMemdWidth,awidth=dataMemaWidth))
  mem.io.wen := io.wen
  mem.io.waddr := io.waddr
  mem.io.wdata := io.wdata
  mem.io.raddr := io.raddr
  io.rdata := mem.io.rdata
  io.memoptvalid := RegNext(io.ren) | RegNext(io.wen)
}
