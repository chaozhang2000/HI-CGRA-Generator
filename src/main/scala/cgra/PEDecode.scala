package cgra
import chisel3._
import chisel3.util._

class PEDecodeIO extends Bundle with CGRAparams{
  val inst = Input(UInt(instMemdWidth.W))
  val alukey= Output(UInt(log2Ceil(aluoptnum).W))
  val src1key = Output(UInt(log2Ceil(srcmuxInputNum).W))
  val src2key = Output(UInt(log2Ceil(srcmuxInputNum).W))
  val haveshiftconst1 = Output(Bool())
  val haveshiftconst2 = Output(Bool())
  val useconst1 = Output(Bool())
  val useconst2 = Output(Bool())
  val linkkey = Output(Vec(pelinkNum,UInt(log2Ceil(crossbarInputNum).W)))
}

class PEDecode extends Module with CGRAparams{
  require(Fukeybits >= log2Ceil(aluoptnum) && Src1keybits >=log2Ceil(srcmuxInputNum)
    && Src2keybits >=log2Ceil(srcmuxInputNum) && Linkkeybits >=log2Ceil(crossbarInputNum))
  val io = IO(new PEDecodeIO)

  io.alukey := io.inst(Fukeystartbit+log2Ceil(aluoptnum)-1,Fukeystartbit)
  io.src1key := io.inst(Src1keystartbit+log2Ceil(srcmuxInputNum)-1,Src1keystartbit)
  io.src2key := io.inst(Src2keystartbit+log2Ceil(srcmuxInputNum)-1,Src2keystartbit)
  for(i <-0 until pelinkNum){
    var linkkeyistartbit = Linkkeystartbit+i*Linkkeybits
    io.linkkey(i) := io.inst(linkkeyistartbit +log2Ceil(crossbarInputNum)-1,linkkeyistartbit)
  }
  io.haveshiftconst1 := io.inst(Shiftconst1startbit)
  io.haveshiftconst2 := io.inst(Shiftconst2startbit)

  io.useconst1 := io.inst(Src1keystartbit+log2Ceil(srcmuxInputNum)-1,Src1keystartbit) === Srcconstcode.U 
  io.useconst2 := io.inst(Src2keystartbit+log2Ceil(srcmuxInputNum)-1,Src2keystartbit) === Srcconstcode.U 
}
