package cgra
import chisel3._
import chisel3.util._

//TODO startcyclecnt update
//TODO valid signal
class CGRA extends Module with CGRAparams{
    val io = IO(new Bundle {
          val wen = Input(Bool())
          val waddr= Input(UInt(awidth.W))
          val wdata = Input(UInt(dwidth.W))
          val finish = Output(Bool())
    })

    val pe0 = Module(new PE)
    val pe1 = Module(new PE)
    val pe2 = Module(new PE)
    val pe3 = Module(new PE)
    val link0 = Module(new Link)
    val link1 = Module(new Link)
    val link2 = Module(new Link)
    val link3 = Module(new Link)
    val link4 = Module(new Link)
    val link5 = Module(new Link)
    val link6 = Module(new Link)
    val link7 = Module(new Link)
    val datamem0 = Module(new Datamem())
    val datamem1 = Module(new Datamem())
    val datamem2 = Module(new Datamem())
    val datamem3 = Module(new Datamem())
          pe0.io.wen := io.wen
          pe0.io.waddr:=io.waddr
          pe0.io.wdata:=io.wdata
          pe1.io.wen :=io.wen
          pe1.io.waddr:=io.waddr
          pe1.io.wdata:=io.wdata
          pe2.io.wen :=io.wen
          pe2.io.waddr:=io.waddr
          pe2.io.wdata:=io.wdata
          pe3.io.wen :=io.wen
          pe3.io.waddr:=io.waddr
          pe3.io.wdata:=io.wdata
    
          (0 until 4).foreach{i =>pe0.io.inLinks(i):= 0.U}
          (0 until 4).foreach{i =>pe1.io.inLinks(i):= 0.U}
          (0 until 4).foreach{i=>pe2.io.inLinks(i):= 0.U}
          (0 until 4).foreach{i =>pe3.io.inLinks(i):= 0.U}
    pe0.io.datamemio <> datamem0.io
    pe1.io.datamemio <> datamem1.io
    pe2.io.datamemio <> datamem2.io
    pe3.io.datamemio <> datamem3.io
    link0.io.in := pe0.io.outLinks(3)
    pe1.io.inLinks(2):= link0.io.out
    link1.io.in := pe1.io.outLinks(2)
    pe0.io.inLinks(3):= link1.io.out
    link2.io.in := pe2.io.outLinks(3)
    pe3.io.inLinks(2):= link2.io.out
    link3.io.in := pe3.io.outLinks(2)
    pe2.io.inLinks(3):= link3.io.out
    link4.io.in := pe0.io.outLinks(0)
    pe2.io.inLinks(1):= link4.io.out
    link5.io.in := pe2.io.outLinks(1)
    pe0.io.inLinks(0):= link5.io.out
    link6.io.in := pe1.io.outLinks(0)
    pe3.io.inLinks(1):= link6.io.out
    link7.io.in := pe3.io.outLinks(1)
    pe1.io.inLinks(0):= link7.io.out
    pe0.io.run :=true.B
    pe1.io.run :=true.B
    pe2.io.run :=true.B
    pe3.io.run :=true.B
    io.finish := pe0.io.finish & pe1.io.finish & pe2.io.finish & pe3.io.finish
}
