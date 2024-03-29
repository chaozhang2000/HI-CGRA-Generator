package cgra
import chisel3._
import chisel3.util._

//TODO startcyclecnt update
//TODO valid signal
class CGRA extends Module with CGRAparams{
    val io = IO(new Bundle {
          val inLinks = Input(Vec(4,UInt(dwidth.W)))
          val outLinks = Output(Vec(4,UInt(dwidth.W)))
          val run = Input(Bool())
          val wen = Input(Bool())
          val waddr= Input(UInt(awidth.W))
          val wdata = Input(UInt(dwidth.W))
    })

    val pe = Module(new PE(4,4))
    val datamem = Module(new Datamem())
    
    pe.io.datamemio <> datamem.io
    pe.io.inLinks := io.inLinks
    io.outLinks := pe.io.outLinks
    pe.io.run := io.run
    pe.io.wen := io.wen
    pe.io.waddr := io.waddr
    pe.io.wdata := io.wdata
}
