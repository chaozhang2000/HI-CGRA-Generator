package cgra
import chisel3._
import chisel3.util._
import scala.collection.mutable.Map

//TODO startcyclecnt update
//TODO valid signal
class PE(inputCount:Int,outputCount:Int) extends Module with CGRAparams{
    val io = IO(new Bundle {
          val inLinks = Input(Vec(inputCount,UInt(dwidth.W)))
          val outLinks = Output(Vec(outputCount,UInt(dwidth.W)))
          val run = Input(Bool())
          val wen = Input(Bool())
          val waddr= Input(UInt(awidth.W))
          val wdata = Input(UInt(dwidth.W))

          val datamemio = Flipped(new DataMemIO)
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
  val Srcmuxs = Seq.tabulate(srcnum){i =>Module(new utils.GenericMux(dWidth =srcmuxWidth ,numOfInputs =srcmuxInputNum))}
  val Alu = Module(new Fu)
  val Crossbar = Module(new utils.Crossbar(inputNum =crossbarInputNum,outputNum =crossbarOutputNum,dataWidth =crossbarDataWidth))

  var regs = Map.empty[Int,UInt]
  (0 until PEctrlregsNum).foreach{i => regs += i->PEctrlregs.io.outData(i)}


  var ctrlregnextmap = Map.empty[Int,UInt]
  var ctrlregwenmap = Map.empty[Int,Bool]
  def regnext1(idcnt:Int,idnum:Int) : (Int,UInt) = {
    idcnt ->Mux((regs(idcnt)<regs(idnum)-1.U)&(regs(idnum)> 0.U),regs(idcnt)+ 1.U,0.U)
  }
  val regnext1cntregs:Seq[Int] = Seq(InstcntIndex,Constcnt1Index,Constcnt2Index,Shiftconstcnt1Index,Shiftconstcnt2Index)
  val regnext1numregs:Seq[Int] = Seq(InstnumIndex,Constnum1Index,Constnum2Index,Shiftconstnum1Index,Shiftconstnum2Index)
  regnext1cntregs.zip(regnext1numregs).foreach{case(cnt,num)=>ctrlregnextmap +=regnext1(cnt,num)}

  ctrlregnextmap +=(IIcntIndex -> Mux(regs(InstcntIndex) ===regs(InstnumIndex)-1.U,regs(IIcntIndex)+ 1.U,regs(IIcntIndex)))
  ctrlregnextmap +=(FinishIndex -> ((regs(IIcntIndex) === regs(FinishIIcntIndex)) & (regs(InstcntIndex)=== regs(FinishInstcntIndex))).asUInt)

  def Loopnuminit(cntID:Int,incID:Int,threadID:Int): Bool = {
    Mux(regs(incID).asSInt>0.S,regs(cntID).asSInt + regs(incID).asSInt >= regs(threadID).asSInt,regs(cntID).asSInt + regs(incID).asSInt <= regs(threadID).asSInt) 
  }
  val Kchange = regs(InstcntIndex) === regs(InstnumIndex) -1.U
  val Kinit = Loopnuminit(KIndex,K_incIndex,K_threadIndex)
  val Knew = Mux(Kinit,regs(K_initIndex),regs(KIndex) + regs(K_incIndex))
  ctrlregnextmap +=(KIndex->Knew)
  val Jchange = Kinit
  val Jinit = Loopnuminit(JIndex,J_incIndex,J_threadIndex)
  val Jnew = Mux(Jinit,regs(J_initIndex),regs(JIndex) + regs(J_incIndex))
  ctrlregnextmap +=(JIndex->Jnew)
  val Ichange = Jinit
  val Iinit = Loopnuminit(IIndex,I_incIndex,I_threadIndex)
  val Inew = Mux(Iinit,regs(I_initIndex),regs(IIndex) + regs(I_incIndex))
  ctrlregnextmap +=(IIndex->Inew)
  val canupdatestate =Decoder.io.canexe //TODO:if Crossbar have less then 4 outputs ?
  ctrlregwenmap +=(InstcntIndex->canupdatestate)
  ctrlregwenmap +=(Constcnt1Index->(canupdatestate&Decoder.io.useconst(0)))
  ctrlregwenmap +=(Constcnt2Index->(canupdatestate&Decoder.io.useconst(1)))
  ctrlregwenmap +=(Shiftconstcnt1Index->(canupdatestate&Decoder.io.haveshiftconst(0)))
  ctrlregwenmap +=(Shiftconstcnt2Index-> (canupdatestate&Decoder.io.haveshiftconst(1)))
  ctrlregwenmap +=(IIcntIndex->canupdatestate)
  ctrlregwenmap +=(FinishIndex->canupdatestate)
  ctrlregwenmap +=(KIndex->(canupdatestate&Kchange))
  ctrlregwenmap +=(JIndex->(canupdatestate&Jchange))
  ctrlregwenmap +=(IIndex->(canupdatestate&Ichange))

  //fureg
  Fureg.io.inData := Alu.io.result.bits
  Fureg.io.enable := canupdatestate & Alu.io.result.valid

  //PEctrlregs
  PEctrlregs.io.configwen := io.wen
  PEctrlregs.io.configwaddr := io.waddr
  PEctrlregs.io.configwdata := io.wdata
  PEctrlregs.io.inData := DontCare
  PEctrlregs.io.wen := DontCare
  PEctrlregs.io.outData:=DontCare


  //Instmems
  Instmems.foreach(_.io.raddr:= Mux(canupdatestate,ctrlregnextmap(InstcntIndex),regs(InstcntIndex)))
  Instmems.zipWithIndex.foreach { case (instMem, i) =>
      instMem.io.waddr := io.waddr - (instMemSize * i).asUInt()
      instMem.io.wen := io.wen && io.waddr >= (instMemStartaddr+instMemSize*i).U && io.waddr < (instMemStartaddr + instMemSize*(i+1)).U
      instMem.io.wdata := io.wdata
  }

  //PEDecode
  Decoder.io.inst.zipWithIndex.foreach{case (instmem,i) => Decoder.io.inst(i):= Instmems(i).io.rdata}
  Decoder.io.iicnt := regs(IIcntIndex)
  Decoder.io.iinum := regs(IInumIndex)
  Decoder.io.startcyclecnt:= regs(StartcyclecntIndex)
  Decoder.io.startcyclenum:= regs(StartcyclenumIndex)

  //Ctrlregs
  ctrlregnextmap.zip(ctrlregwenmap).foreach{case(next,wen)=> 
    require(next._1 == wen._1)
    PEctrlregs.io.inData(next._1):= next._2
    PEctrlregs.io.wen(next._1):= wen._2 
  }

  //Constmems
  Constmems(0).io.raddr:=Mux(canupdatestate,ctrlregnextmap(Constcnt1Index),regs(Constcnt1Index))
  Constmems(1).io.raddr:=Mux(canupdatestate,ctrlregnextmap(Constcnt2Index),regs(Constcnt2Index))
  Constmems.zipWithIndex.foreach { case (constMem, i) =>
      constMem.io.waddr := io.waddr - (constMemSize * i).asUInt()
      constMem.io.wen := io.wen && io.waddr >= (constMemStartaddr+constMemSize*i).U && io.waddr < (constMemStartaddr + constMemSize*(i+1)).U
      constMem.io.wdata := io.wdata
  }

  //ShiftConstmems
  Shiftconstmems(0).io.raddr:= Mux(canupdatestate,ctrlregnextmap(Shiftconstcnt1Index),regs(Shiftconstcnt1Index))
  Shiftconstmems(1).io.raddr:= Mux(canupdatestate,ctrlregnextmap(Shiftconstcnt2Index),regs(Shiftconstcnt2Index))
  Shiftconstmems.zipWithIndex.foreach { case (shiftconstMem, i) =>
      shiftconstMem.io.waddr := io.waddr - (shiftconstMemSize * i).asUInt()
      shiftconstMem.io.wen := io.wen && io.waddr >= (shiftconstMemStartaddr+shiftconstMemSize*i).U && io.waddr < (shiftconstMemStartaddr + shiftconstMemSize*(i+1)).U
      shiftconstMem.io.wdata := io.wdata
  }

  Srcmuxs.zipWithIndex.foreach {case(srcmux,i) => 
    srcmux.io.sel := Decoder.io.srckey(i)
    srcmux.io.in(0):= Fureg.io.outData
    srcmux.io.in(1):= Constmems(0).io.rdata
    io.inLinks.zipWithIndex.foreach{case(link,i)=>srcmux.io.in(i+2):=link}
    srcmux.io.in(6):= regs(IIndex)
    srcmux.io.in(7):= regs(JIndex)
    srcmux.io.in(8):= regs(KIndex)
  }
  //FU
  Alu.io.fn := Decoder.io.alukey
  Alu.io.src1:=Mux(Decoder.io.haveshiftconst(0),(Srcmuxs(0).io.out.asSInt +Shiftconstmems(0).io.rdata.asSInt).asUInt,Srcmuxs(0).io.out)
  Alu.io.src2:=Mux(Decoder.io.haveshiftconst(1),(Srcmuxs(1).io.out.asSInt +Shiftconstmems(1).io.rdata.asSInt).asUInt,Srcmuxs(1).io.out)
  
  //Crossbar
  Crossbar.io.select := Decoder.io.linkkey
  Crossbar.io.in(0):=0.U
  io.inLinks.zipWithIndex.foreach{case(link,i)=>Crossbar.io.in(i+1):=link}
  Crossbar.io.in(5):=Alu.io.result.bits
  Crossbar.io.in(6):=Fureg.io.outData
  io.outLinks:=Crossbar.io.out.map(signal => {
    val updateout = Wire(UInt(crossbarDataWidth.W))
    updateout:= signal
    updateout
  })

  //datamem
  io.datamemio <> Alu.io.datamemio
}
