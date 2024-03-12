package utils
import chisel3._
import chisel3.util._
  class Memutil(depth:Int,dwidth:Int,awidth:Int,syncRead:Boolean = true)extends Module {
    require(depth >0 && dwidth >0 && awidth >0)
  val io = IO(new Bundle{
    val wen   = Input(Bool())
    val waddr= Input(UInt(awidth.W))
    val wdata = Input(UInt(dwidth.W))
    val raddr = Input(UInt(awidth.W))
    val rdata = Output(UInt(dwidth.W))
  })
    val mem = if (syncRead==true) {SyncReadMem(depth, UInt(dwidth.W))} else {Mem(depth,UInt(dwidth.W))}
      io.rdata := mem.read(io.raddr)
      when(io.wen) {
        mem.write(io.waddr, io.wdata) // 在指定地址写入数据
      }
}
