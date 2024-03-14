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

          //test 
  val src1key = Output(UInt(log2Ceil(srcmuxInputNum).W))
  val src2key = Output(UInt(log2Ceil(srcmuxInputNum).W))
  val alukey= Output(UInt(log2Ceil(aluoptnum).W))
    val const1raddr = Output(UInt(awidth.W))
    val const2raddr = Output(UInt(awidth.W))
  val useconst1 = Output(Bool())
  val useconst2 = Output(Bool())
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
  val Instcntreg = PEctrlregs.io.outData(InstcntIndex)
  val Instnumreg = PEctrlregs.io.outData(InstnumIndex)
  val Constcnt1reg = PEctrlregs.io.outData(Constcnt1Index)
  val Constcnt2reg = PEctrlregs.io.outData(Constcnt2Index)
  val Constnum1reg = PEctrlregs.io.outData(Constnum1Index)
  val Constnum2reg = PEctrlregs.io.outData(Constnum2Index)
  val Shiftconstcnt1reg = PEctrlregs.io.outData(Shiftconstcnt1Index)
  val Shiftconstcnt2reg = PEctrlregs.io.outData(Shiftconstcnt2Index)
  val Shiftconstnum1reg = PEctrlregs.io.outData(Shiftconstnum1Index)
  val Shiftconstnum2reg = PEctrlregs.io.outData(Shiftconstnum2Index)

  //Instcntreg
  val instcntregnext = Mux((Instcntreg<Instnumreg-1.U)&(Instnumreg > 0.U),Instcntreg + 1.U,0.U)
  PEctrlregs.io.inData(InstcntIndex):=instcntregnext
  PEctrlregs.io.wen(InstcntIndex):= io.run//TODO not only io.run
  io.rdata(10) := Instcntreg

  //Instmems
  Instmems.foreach(_.io.raddr:= Mux(io.run,instcntregnext,Instcntreg))//TODO not io.run
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
  val constcnt1regnext = Mux((Constcnt1reg < Constnum1reg-1.U)&(Constnum1reg > 0.U),Constcnt1reg + 1.U,0.U)
  PEctrlregs.io.inData(Constcnt1Index) := constcnt1regnext
  PEctrlregs.io.wen(Constcnt1Index):= io.run &(Decoder.io.useconst1)//TODO
  //Constcnt2reg
  val constcnt2regnext = Mux((Constcnt2reg < Constnum2reg-1.U)&(Constnum2reg > 0.U),Constcnt2reg + 1.U,0.U)
  PEctrlregs.io.inData(Constcnt2Index) := constcnt2regnext
  PEctrlregs.io.wen(Constcnt2Index):= io.run &(Decoder.io.useconst2)//TODO
  //shiftConstcnt1reg
  val shiftconstcnt1regnext = Mux((Shiftconstcnt1reg < Shiftconstnum1reg - 1.U)&(Shiftconstnum1reg>0.U ),Shiftconstcnt1reg + 1.U,0.U)
  PEctrlregs.io.inData(Shiftconstcnt1Index) := shiftconstcnt1regnext
  PEctrlregs.io.wen(Shiftconstcnt1Index):= io.run &(Decoder.io.haveshiftconst1)//TODO
  //shiftConstcnt2reg
  val shiftconstcnt2regnext = Mux((Shiftconstcnt2reg < Shiftconstnum2reg-1.U)&(Shiftconstnum2reg>0.U ),Shiftconstcnt2reg + 1.U,0.U)
  PEctrlregs.io.inData(Shiftconstcnt2Index) := shiftconstcnt2regnext
  PEctrlregs.io.wen(Shiftconstcnt2Index):= io.run &(Decoder.io.haveshiftconst2)//
  //Constmems
  Constmems(0).io.raddr:=Mux(io.run,constcnt1regnext,Constcnt1reg)//TODO not io.run
  Constmems(1).io.raddr:=Mux(io.run,constcnt2regnext,Constcnt2reg)//TODO not iorun.
  Constmems.zipWithIndex.foreach { case (constMem, i) =>
      constMem.io.waddr := io.waddr - (constMemSize * i).asUInt()
      constMem.io.wen := io.wen && io.waddr >= (constMemStartaddr+constMemSize*i).U && io.waddr < (constMemStartaddr + constMemSize*(i+1)).U
      constMem.io.wdata := io.wdata
  }
  io.rdata(6) := Constmems(0).io.rdata
  io.rdata(7) := Constmems(1).io.rdata

  //ShiftConstmems
  Shiftconstmems(0).io.raddr:= Mux(io.run,shiftconstcnt1regnext,Shiftconstcnt1reg)//TODO not io.run
  Shiftconstmems(1).io.raddr:= Mux(io.run,shiftconstcnt2regnext,Shiftconstcnt2reg)//TODO not io.run
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

  //test
  io.src1key := Decoder.io.src1key
  io.src2key := Decoder.io.src2key
  io.alukey := Decoder.io.alukey
  io.const1raddr := constcnt1regnext
  io.const2raddr := constcnt2regnext
  io.useconst1:=Decoder.io.useconst1
  io.useconst2:=Decoder.io.useconst2
}
