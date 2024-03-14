package cgra

import chisel3._
import chisel3.util._

object AddMain extends App with CGRAparams{
  println("Generating the alu hardware")
  emitVerilog(new utils.Alu(aluwidth,aluoptnum,aluoptlist) , Array("--target-dir", "generated"))
  println("Generating the aluvalid hardware")
  emitVerilog(new utils.Aluvalid(aluwidth,aluoptnum,aluoptlist) , Array("--target-dir", "generated"))

  println("Generating the Crossbarvalid hardware")
  emitVerilog(new utils.Crossbarvalid(crossbarInputNum,crossbarOutputNum,crossbarDataWidth),Array("--target-dir", "generated"))

  println("Generating the Crossbar hardware")
  emitVerilog(new utils.Crossbar(crossbarInputNum,crossbarOutputNum,crossbarDataWidth),Array("--target-dir", "generated"))

  println("Generating the instMem hardware")
  emitVerilog(new utils.Memutil(depth = instMemSize,dwidth = instMemdWidth,awidth = instMemaWidth),Array("--target-dir", "generated/instmem"))

  println("Generating the SrcMux")
  emitVerilog(new utils.Muxvalid(srcmuxWidth,srcmuxInputNum),Array("--target-dir", "generated"))

  println("Generating the Reg")
  emitVerilog(new utils.Register(dwidth,0.U),Array("--target-dir", "generated"))

  println("Generating the Regvalid")

  emitVerilog(new utils.Regvalid(dwidth,0.U,false.B),Array("--target-dir", "generated"))

  println("Generating the PEDecode")
  emitVerilog(new cgra.PEDecode(),Array("--target-dir", "generated"))

  println("Generating the PEctrlregs")
  emitVerilog(new cgra.PEctrlregs(),Array("--target-dir", "generated"))

  println("Generating the PE")
  emitVerilog(new cgra.PE(4,4),Array("--target-dir", "generated"))
}
