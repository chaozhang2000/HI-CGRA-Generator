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

          val aluresult = Output(UInt(aluwidth.W))
    })
  val PEctrlregs = Module(new PEctrlregs)
  val Fureg = Module(new utils.Register(width = aluwidth,resetValue = 0.U))
  val Instmems = Seq.tabulate(instmemNum) { i =>
    Module(new utils.Memutil(depth = instMemSize,dwidth = instMemdWidth,awidth = instMemaWidth))
  }
  val Decoder = Module(new PEDecode)
  val Constmems = Seq.tabulate(constmemNum){ i => 
    Module(new utils.Memutil(depth = constMemSize,dwidth = constMemdWidth,awidth = constMemaWidth))
  }
  val Shiftconstmems = Seq.tabulate(shiftconstmemNum){ i => 
    Module(new utils.Memutil(depth = shiftconstMemSize,dwidth = shiftconstMemdWidth,awidth = shiftconstMemaWidth))
  }
  val Src1mux = Module(new utils.GenericMux(dWidth =srcmuxWidth ,numOfInputs =srcmuxInputNum))
  val Src2mux = Module(new utils.GenericMux(dWidth = srcmuxWidth,numOfInputs =srcmuxInputNum))
  val Alu = Module(new utils.Alu(dataWidth =aluwidth ,functionNum=aluoptnum ,opneed=aluoptlist))
  val Crossbar = Module(new utils.Crossbar(inputNum =crossbarInputNum,outputNum =crossbarOutputNum,dataWidth =crossbarDataWidth))

  //fureg
  Fureg.io.inData := Alu.io.result.asUInt
  Fureg.io.enable := io.run
  io.aluresult := Fureg.io.outData

  //PEctrlregs
  PEctrlregs.io.configwen := io.wen
  PEctrlregs.io.configwaddr := io.waddr
  PEctrlregs.io.configwdata := io.wdata
  PEctrlregs.io.inData := DontCare
  PEctrlregs.io.wen := DontCare
  PEctrlregs.io.outData:=DontCare

  //Instcntreg
  val instcntregnext = Mux(PEctrlregs.io.outData(InstcntIndex)<7.U,PEctrlregs.io.outData(InstcntIndex) + 1.U,0.U)//TODO 7U should be Instnum
  PEctrlregs.io.inData(InstcntIndex):= instcntregnext
  PEctrlregs.io.wen(InstcntIndex):= io.run//TODO not only io.run
  io.rdata(10) := PEctrlregs.io.outData(InstcntIndex)

  //Instmems
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

  //PEDecode
  Decoder.io.inst := Instmems(0).io.rdata

  //Constcnt1reg
  val constcnt1regnext = Mux(PEctrlregs.io.outData(Constcnt1Index)<7.U,PEctrlregs.io.outData(Constcnt1Index) + 1.U,0.U)//TODO 7U should be constnum1
  PEctrlregs.io.inData(Constcnt1Index) := constcnt1regnext
  PEctrlregs.io.wen(Constcnt1Index):= io.run &(Decoder.io.useconst1)//
  //Constcnt2reg
  val constcnt2regnext = Mux(PEctrlregs.io.outData(Constcnt2Index)<7.U,PEctrlregs.io.outData(Constcnt2Index) + 1.U,0.U)//TODO 7U should be constnum1
  PEctrlregs.io.inData(Constcnt2Index) := constcnt2regnext
  PEctrlregs.io.wen(Constcnt2Index):= io.run &(Decoder.io.useconst2)//
  //shiftConstcnt1reg
  val shiftconstcnt1regnext = Mux(PEctrlregs.io.outData(Shiftconstnum1Index)<7.U,PEctrlregs.io.outData(Shiftconstnum1Index) + 1.U,0.U)//TODO 7U should be constnum1
  PEctrlregs.io.inData(Shiftconstnum1Index) := shiftconstcnt1regnext
  PEctrlregs.io.wen(Shiftconstnum1Index):= io.run &(Decoder.io.haveshiftconst1)//
  //shiftConstcnt2reg
  val shiftconstcnt2regnext = Mux(PEctrlregs.io.outData(Shiftconstnum2Index)<7.U,PEctrlregs.io.outData(Shiftconstnum2Index) + 1.U,0.U)//TODO 7U should be constnum1
  PEctrlregs.io.inData(Shiftconstnum2Index) := shiftconstcnt2regnext
  PEctrlregs.io.wen(Shiftconstnum2Index):= io.run &(Decoder.io.haveshiftconst2)//
  //Constmems
  Constmems(0).io.raddr:=constcnt1regnext
  Constmems(1).io.raddr:=constcnt2regnext
  Constmems.zipWithIndex.foreach { case (constMem, i) =>
      constMem.io.waddr := io.waddr - (constMemSize * i).asUInt()
      constMem.io.wen := io.wen && io.waddr >= (constMemStartaddr+constMemSize*i).U && io.waddr < (constMemStartaddr + constMemSize*(i+1)).U
      constMem.io.wdata := io.wdata
  }
  io.rdata(6) := Constmems(0).io.rdata
  io.rdata(7) := Constmems(1).io.rdata

  //ShiftConstmems
  Shiftconstmems.foreach(_.io.raddr:= instcntregnext)//TODO: should not be Instcntreg
  Shiftconstmems.zipWithIndex.foreach { case (shiftconstMem, i) =>
      shiftconstMem.io.waddr := io.waddr - (shiftconstMemSize * i).asUInt()
      shiftconstMem.io.wen := io.wen && io.waddr >= (shiftconstMemStartaddr+shiftconstMemSize*i).U && io.waddr < (shiftconstMemStartaddr + shiftconstMemSize*(i+1)).U
      shiftconstMem.io.wdata := io.wdata
  }
  io.rdata(8) := Shiftconstmems(0).io.rdata
  io.rdata(9) := Shiftconstmems(1).io.rdata

  //src1mux
  Src1mux.io.sel := Decoder.io.src1key
  Src1mux.io.in(0):= Fureg.io.outData
  Src1mux.io.in(1):= Constmems(0).io.rdata
  io.inLinks.zipWithIndex.foreach{case(link,i)=>Src1mux.io.in(i+2):=link}
  Src1mux.io.in(6):= 0.U //TODO: loop0
  Src1mux.io.in(7):= 0.U //TODO: loop1
  Src1mux.io.in(8):= 0.U //TODO: loop2


  //src2mux
  Src2mux.io.sel := Decoder.io.src2key
  Src2mux.io.in(0):= Fureg.io.outData
  Src2mux.io.in(1):= Constmems(1).io.rdata
  io.inLinks.zipWithIndex.foreach{case(link,i)=>Src2mux.io.in(i+2):=link}
  Src2mux.io.in(6):= 0.U //TODO: loop0
  Src2mux.io.in(7):= 0.U //TODO: loop1
  Src2mux.io.in(8):= 0.U //TODO: loop2
  //FU
  Alu.io.fn := Decoder.io.alukey
  Alu.io.src1:=Mux(Decoder.io.haveshiftconst1,Src1mux.io.out.asSInt +Shiftconstmems(0).io.rdata.asSInt ,Src1mux.io.out.asSInt)
  Alu.io.src2:=Mux(Decoder.io.haveshiftconst2,Src2mux.io.out.asSInt +Shiftconstmems(1).io.rdata.asSInt ,Src2mux.io.out.asSInt)
  
  //Crossbar
  Crossbar.io.select := Decoder.io.linkkey
  Crossbar.io.in(0):=0.U
  io.inLinks.zipWithIndex.foreach{case(link,i)=>Crossbar.io.in(i+1):=link}
  Crossbar.io.in(5):=Alu.io.result.asUInt
  Crossbar.io.in(6):=Fureg.io.outData
  io.outLinks:=Crossbar.io.out
}
