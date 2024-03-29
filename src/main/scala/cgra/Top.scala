package cgra

import chisel3._
import chisel3.util._

object AddMain extends App with CGRAparams{
  println("Generating the alu hardware")
  emitVerilog(new cgra.Fu(), Array("--target-dir", "generated"))

  println("Generating the Crossbarvalid hardware")
  emitVerilog(new utils.Crossbarvalid(crossbarInputNum,crossbarOutputNum,crossbarDataWidth),Array("--target-dir", "generated"))

  println("Generating the Crossbar hardware")
  emitVerilog(new utils.Crossbar(crossbarInputNum,crossbarOutputNum,crossbarDataWidth),Array("--target-dir", "generated"))

  println("Generating the instMem hardware")
  emitVerilog(new utils.Memutil(depth = instMemSize,dwidth = instMemdWidth,awidth = instMemaWidth),Array("--target-dir", "generated/instmem"))

  println("Generating the SrcMux")
  emitVerilog(new utils.Muxvalid(srcmuxWidth,srcmuxInputNum),Array("--target-dir", "generated"))

  println("Generating the onehot")
  emitVerilog(new utils.Muxonehot(srcmuxWidth,srcmuxInputNum),Array("--target-dir", "generated"))

  println("Generating the Reg")
  emitVerilog(new utils.Register(dwidth,0.U),Array("--target-dir", "generated"))

  println("Generating the Regvalid")

  emitVerilog(new utils.Regvalid(dwidth,0.U,false.B),Array("--target-dir", "generated"))

  println("Generating the PEDecode")
  emitVerilog(new cgra.PEDecode(),Array("--target-dir", "generated"))

  println("Generating the PEctrlregs")
  emitVerilog(new cgra.PEctrlregs(),Array("--target-dir", "generated"))

  println("Generating the Datamem")
  emitVerilog(new cgra.Datamem(),Array("--target-dir", "generated"))

  println("Generating the CGRA")
  emitVerilog(new cgra.CGRA(),Array("--target-dir", "generated"))

  println("Generating the PE")
  emitVerilog(new cgra.PE(),Array("--target-dir", "generated"))

  println("Generating the Link")
  emitVerilog(new cgra.Link(),Array("--target-dir", "generated"))

  println("Generating the pipe")
  emitVerilog(new utils.PipelineConnectTest,Array("--target-dir", "generated"))
}
