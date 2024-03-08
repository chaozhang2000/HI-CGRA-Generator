package cgra
import chisel3._
import chisel3.util._
class PE(inputCount:Int,outputCount:Int) extends Module with top.CGRAparams{
    val io = IO(new Bundle {
          val inLinks = Input(Vec(inputCount,Valid(SInt(dwidth.W))))
          val outLinks = Output(Vec(inputCount,Valid(SInt(dwidth.W))))
          val wen = Input(Bool())
          val waddr= Input(UInt(awidth.W))
          val wdata = Input(UInt(dwidth.W))
          val rdata = Output(Vec(6,UInt(dwidth.W)))
          
    })
  val Instcntreg = Module(new utils.Register(dwidth,0.S))
  val Instmems = Seq.tabulate(2+pelinkNum) { i =>
    Module(new utils.Memutil(depth = instMemSize,dwidth = instMemdWidth,awidth = instMemaWidth))
  }
  Instmems.foreach(_.io.raddr(0):= Instcntreg.io.inData.asUInt())
  Instmems.zipWithIndex.foreach { case (instMem, i) =>
      instMem.io.waddr(0) := io.waddr - (instMemSize * i).asUInt()
      instMem.io.wen(0) := io.wen && io.waddr >= (instMemStartaddr+instMemSize*i).U && io.waddr < (instMemStartaddr + instMemSize*(i+1)).U
      instMem.io.wdata(0) := io.wdata
  }
  Instcntreg.io.enable := true.B
  Instcntreg.io.inData := Mux(Instcntreg.io.outData<7.S,Instcntreg.io.outData + 1.S,0.S)//TODO
  io.rdata(0) := Instmems(0).io.rdata(0)
  io.rdata(1) := Instmems(1).io.rdata(0)
  io.rdata(2) := Instmems(2).io.rdata(0)
  io.rdata(3) := Instmems(3).io.rdata(0)
  io.rdata(4) := Instmems(4).io.rdata(0)
  io.rdata(5) := Instmems(5).io.rdata(0)
  io.outLinks := io.inLinks
}
