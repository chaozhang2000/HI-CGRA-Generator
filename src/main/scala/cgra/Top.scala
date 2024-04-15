package cgra

import chisel3._
import chisel3.util._

object AddMain extends App with CGRAparams{
  println("Generating the CGRA")
  emitVerilog(new cgra.CGRA(),Array("--target-dir", "generated"))
}
