package cgra
import chisel3._
import chisel3.util._

class PEDecodeIO extends Bundle with CGRAparams{
  val inst = Input(Vec(instmemNum,UInt(instMemdWidth.W)))
  val iicnt = Input(UInt(PEctrlregsdWidth.W))
  val iinum = Input(UInt(PEctrlregsdWidth.W))
  val startcyclecnt= Input(UInt(PEctrlregsdWidth.W))
  val startcyclenum= Input(UInt(PEctrlregsdWidth.W))
  val alukey= Output(UInt(log2Ceil(aluoptnum).W))
  val srckey = Output(Vec(srcnum,UInt(log2Ceil(srcmuxInputNum).W)))
  val linkkey = Output(Vec(pelinkNum,UInt(log2Ceil(crossbarInputNum).W)))
  val useconst = Output(Vec(srcnum,Bool()))
  val haveshiftconst = Output(Vec(srcnum,Bool()))
  val linkneedtosendout =Output(Vec(crossbarOutputNum,Bool())) 
  val fuinstskip = Output(Bool())
  val linkinstskip = Output(Vec(crossbarOutputNum,Bool())) 
  val canexe = Output(Bool())
}

class PEDecode extends Module with CGRAparams{
  require(Fukeybits >= log2Ceil(aluoptnum) && Src1keybits >=log2Ceil(srcmuxInputNum)
    && Src2keybits >=log2Ceil(srcmuxInputNum) && Linkkeybits >=log2Ceil(crossbarInputNum))
  val io = IO(new PEDecodeIO)

  io.alukey := io.inst(instmemindex)(Fukeystartbit+log2Ceil(aluoptnum)-1,Fukeystartbit)
  io.srckey(0) := io.inst(instmemindex)(Src1keystartbit+log2Ceil(srcmuxInputNum)-1,Src1keystartbit)
  io.srckey(1) := io.inst(instmemindex)(Src2keystartbit+log2Ceil(srcmuxInputNum)-1,Src2keystartbit)
  for(i <-0 until pelinkNum){
    var linkkeyistartbit = Linkkeystartbit+i*Linkkeybits
    var linkkey = io.inst(instmemindex)(linkkeyistartbit +log2Ceil(crossbarInputNum)-1,linkkeyistartbit)
    io.linkkey(i) := linkkey
    io.linkneedtosendout(i) := (linkkey != Linkemptycode.U).asBool
  }

  io.useconst(0):= io.inst(instmemindex)(Src1keystartbit+log2Ceil(srcmuxInputNum)-1,Src1keystartbit) === Srcconstcode.U 
  io.useconst(1):= io.inst(instmemindex)(Src2keystartbit+log2Ceil(srcmuxInputNum)-1,Src2keystartbit) === Srcconstcode.U 

  io.haveshiftconst(0) := io.inst(instmemindex)(Shiftconst1startbit)
  io.haveshiftconst(1) := io.inst(instmemindex)(Shiftconst2startbit)

  io.fuinstskip := (io.iicnt < io.inst(fudelayindex)) ||(io.iicnt >= io.iinum +io.inst(fudelayindex))

  (0 until pelinkNum).foreach{ i => io.linkinstskip(i) := (io.iicnt < io.inst(linkdelayindex + i)) ||(io.iicnt >= io.iinum +io.inst(linkdelayindex + i))}

  io.canexe := io.startcyclecnt >= io.startcyclenum
}
