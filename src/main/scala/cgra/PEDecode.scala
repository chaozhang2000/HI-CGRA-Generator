package cgra
import chisel3._
import chisel3.util._

class PEDecodeIO extends Bundle with CGRAparams{
  val inst = Input(UInt(instMemdWidth.W))
  val alukey= Output(UInt(log2Ceil(aluoptnum+1).W))
  val src1key = Output(UInt(Src1keybits.W))
  val src2key = Output(UInt(Src2keybits.W))
  val haveshiftconst1 = Output(Bool())
  val haveshiftconst2 = Output(Bool())
  val linkkey = Output(Vec(pelinkNum,UInt(Linkkeybits.W)))
}
