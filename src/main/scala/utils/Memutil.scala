package utils
import chisel3._
import chisel3.util._
  class Memutil(readPorts:Int,writePorts:Int,depth:Int,width:Int,syncRead:Boolean)extends Module {
    require(readPorts >= 0 &&writePorts >=0 && depth >0 && width >0)
  val io = IO(new Bundle{
    val wen   = Input(Vec(writePorts,Bool()))
    val waddr= Input(Vec(writePorts, UInt(log2Ceil(depth).W)))
    val wdata = Input(Vec(writePorts, UInt(width.W)))
    val raddr = Input(Vec(readPorts, UInt(log2Ceil(depth).W)))
    val rdata = Output(Vec(readPorts, UInt(width.W)))
  })
    val mem = if (syncRead==true) {SyncReadMem(depth, UInt(width.W))} else {Mem(depth,UInt(width.W))}
    for (i <- 0 until readPorts){
      io.rdata(i) := mem.read(io.raddr(i))
    }
    for (i <- 0 until writePorts){
      when(io.wen(i)) {
        mem.write(io.waddr(i), io.wdata(i)) // 在指定地址写入数据
      }
    }
}
