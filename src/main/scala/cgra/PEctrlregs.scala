package cgra
import chisel3._
import chisel3.util._

class PEctrlregs extends Module with CGRAparams{
    val io = IO(new Bundle {
          val inData = Input(Vec(PEctrlregsNum,UInt(PEctrlregsdWidth.W)))
          val outData = Output(Vec(PEctrlregsNum,UInt(PEctrlregsdWidth.W)))
          val wen = Input(Vec(PEctrlregsNum,Bool()))
          val configwaddr= Input(UInt(PEctrlregsaWidth.W))
          val configwen = Input(Bool())
          val configwdata = Input(UInt(PEctrlregsdWidth.W))

    })
  val Ctrlregs = Seq.tabulate(PEctrlregsNum) { i => Module(new utils.Register(PEctrlregsdWidth,0.U))}
  Ctrlregs.zipWithIndex.foreach { case (ctrlreg, i) =>
    ctrlreg.io.inData :=Mux(io.configwen,io.configwdata,io.inData(i))
    io.outData(i) := ctrlreg.io.outData
    ctrlreg.io.enable := io.wen(i) | (io.configwen & (io.configwaddr === (PEctrlregsStartaddr + i).U))
  }
}
