package utils
import chisel3._
import chisel3.util._
  class MemMulPort(readPorts:Int = 1,writePorts:Int = 1,depth:Int,dwidth:Int,awidth:Int,syncRead:Boolean = true)extends Module {
    require(readPorts > 0 &&writePorts >0 && depth >0 && dwidth >0 && awidth >0)
  val io = IO(new Bundle{
    val wen   = Input(Vec(writePorts,Bool()))
    val waddr= Input(Vec(writePorts, UInt(awidth.W)))
    val wdata = Input(Vec(writePorts, UInt(dwidth.W)))
    val raddr = Input(Vec(readPorts, UInt(awidth.W)))
    val rdata = Output(Vec(readPorts, UInt(dwidth.W)))
  })
    val mem = if (syncRead==true) {SyncReadMem(depth, UInt(dwidth.W))} else {Mem(depth,UInt(dwidth.W))}
    for (i <- 0 until readPorts){
      io.rdata(i) := mem.read(io.raddr(i))
    }
    for (i <- 0 until writePorts){
      when(io.wen(i)) {
        mem.write(io.waddr(i), io.wdata(i)) // 在指定地址写入数据
      }
    }
}
