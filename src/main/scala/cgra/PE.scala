package cgra
import chisel3._
import chisel3.util._

class PE(inputCount:Int,outputCount:Int) extends Module with CGRAparams{
    val io = IO(new Bundle {
          //val inLinks = Input(Vec(inputCount,Valid(SInt(dwidth.W))))
          //val outLinks = Output(Vec(outputCount,Valid(SInt(dwidth.W))))
          val run = Input(Bool())
          val inLinks = Input(Vec(inputCount,UInt(dwidth.W)))
          val outLinks = Output(Vec(outputCount,UInt(dwidth.W)))
          val wen = Input(Bool())
          val waddr= Input(UInt(awidth.W))
          val wdata = Input(UInt(dwidth.W))
          val rdata = Output(Vec(6+2+2+1,UInt(dwidth.W)))
  //decode out IO test
  val alukey= Output(UInt(log2Ceil(aluoptnum).W))
  val src1key = Output(UInt(log2Ceil(srcmuxInputNum).W))
  val src2key = Output(UInt(log2Ceil(srcmuxInputNum).W))
  val haveshiftconst1 = Output(Bool())
  val haveshiftconst2 = Output(Bool())
    })
  //PEctrlregs
  val PEctrlregs = Module(new PEctrlregs)
  PEctrlregs.io.configwen := io.wen
  PEctrlregs.io.configwaddr := io.waddr
  PEctrlregs.io.configwdata := io.wdata
  PEctrlregs.io.inData := DontCare
  PEctrlregs.io.wen := DontCare
  PEctrlregs.io.outData:=DontCare

  //Instcntreg
  val instcntregnext = Mux(PEctrlregs.io.outData(InstcntIndex)<7.U,PEctrlregs.io.outData(InstcntIndex) + 1.U,0.U)//TODO 7U
  PEctrlregs.io.inData(InstcntIndex):= instcntregnext
  PEctrlregs.io.wen(InstcntIndex):= io.run//TODO not only io.run
  io.rdata(10) := PEctrlregs.io.outData(InstcntIndex)
  
  //Instmems
  val Instmems = Seq.tabulate(instmemNum) { i =>
    Module(new utils.Memutil(depth = instMemSize,dwidth = instMemdWidth,awidth = instMemaWidth))
  }
  Instmems.foreach(_.io.raddr:= instcntregnext)
  Instmems.zipWithIndex.foreach { case (instMem, i) =>
      instMem.io.waddr := io.waddr - (instMemSize * i).asUInt()
      instMem.io.wen := io.wen && io.waddr >= (instMemStartaddr+instMemSize*i).U && io.waddr < (instMemStartaddr + instMemSize*(i+1)).U
      instMem.io.wdata := io.wdata
  }
  io.rdata(0) := Instmems(0).io.rdata
  io.rdata(1) := Instmems(1).io.rdata
  io.rdata(2) := Instmems(2).io.rdata
  io.rdata(3) := Instmems(3).io.rdata
  io.rdata(4) := Instmems(4).io.rdata
  io.rdata(5) := Instmems(5).io.rdata
  
  //Constmems
  val Constmems = Seq.tabulate(constmemNum){ i => 
    Module(new utils.Memutil(depth = constMemSize,dwidth = constMemdWidth,awidth = constMemaWidth))
  }
  Constmems.foreach(_.io.raddr:= instcntregnext)//TODO: should not be Instcntreg
  Constmems.zipWithIndex.foreach { case (constMem, i) =>
      constMem.io.waddr := io.waddr - (constMemSize * i).asUInt()
      constMem.io.wen := io.wen && io.waddr >= (constMemStartaddr+constMemSize*i).U && io.waddr < (constMemStartaddr + constMemSize*(i+1)).U
      constMem.io.wdata := io.wdata
  }
  io.rdata(6) := Constmems(0).io.rdata
  io.rdata(7) := Constmems(1).io.rdata

  //ShiftConstmems
  val Shiftconstmems = Seq.tabulate(shiftconstmemNum){ i => 
    Module(new utils.Memutil(depth = shiftconstMemSize,dwidth = shiftconstMemdWidth,awidth = shiftconstMemaWidth))
  }
  Shiftconstmems.foreach(_.io.raddr:= instcntregnext)//TODO: should not be Instcntreg
  Shiftconstmems.zipWithIndex.foreach { case (shiftconstMem, i) =>
      shiftconstMem.io.waddr := io.waddr - (shiftconstMemSize * i).asUInt()
      shiftconstMem.io.wen := io.wen && io.waddr >= (shiftconstMemStartaddr+shiftconstMemSize*i).U && io.waddr < (shiftconstMemStartaddr + shiftconstMemSize*(i+1)).U
      shiftconstMem.io.wdata := io.wdata
  }
  io.rdata(8) := Shiftconstmems(0).io.rdata
  io.rdata(9) := Shiftconstmems(1).io.rdata

  //PEDecode
  val Decoder = Module(new PEDecode)
  Decoder.io.inst := Instmems(0).io.rdata
  io.alukey:=Decoder.io.alukey
  io.src1key := Decoder.io.src1key
  io.src2key := Decoder.io.src2key
  io.haveshiftconst1 := Decoder.io.haveshiftconst1
  io.haveshiftconst2 := Decoder.io.haveshiftconst2
  
  //Crossbar
  val Crossbar = Module(new utils.Crossbar(inputNum =crossbarInputNum,outputNum =crossbarOutputNum,dataWidth =crossbarDataWidth))
  Crossbar.io.select := Decoder.io.linkkey
  Crossbar.io.in(0):=0.U
  io.inLinks.zipWithIndex.foreach{case(link,i)=>Crossbar.io.in(i+1):=link}
  Crossbar.io.in(5):=0.U
  Crossbar.io.in(6):=0.U
  io.outLinks:=Crossbar.io.out
}
