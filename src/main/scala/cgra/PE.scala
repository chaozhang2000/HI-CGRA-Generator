package cgra
import chisel3._
import chisel3.util._
import utils.PipelineConnect
import scala.collection.mutable.Map

class PE (ID:Int)extends Module with CGRAparams{
    val io = IO(new Bundle {
          val inLinks = Input(Vec(pelinkNum,UInt(dwidth.W)))
          val outLinks = Output(Vec(pelinkNum,Valid(UInt(dwidth.W))))
          val run = Input(Bool())
          val wen = Input(Bool())
          val waddr= Input(UInt(awidth.W))
          val wdata = Input(UInt(dwidth.W))
          val finish = Output(Bool())

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
  val Alu = Module(new Fu(ID))
  val Crossbar = Module(new utils.Crossbar(inputNum =crossbarInputNum,outputNum =crossbarOutputNum,dataWidth =crossbarDataWidth))

  var regs = Map.empty[Int,UInt]
  (0 until PEctrlregsNum).foreach{i => regs += i->PEctrlregs.io.outData(i)}

  //pipeline wire
  val alukeypipe= Wire(UInt(log2Ceil(aluoptnum).W))
  val srckeypipe = Wire(Vec(srcnum,UInt(log2Ceil(srcmuxInputNum).W)))
  val linkkeypipe = Wire(Vec(pelinkNum,UInt(log2Ceil(crossbarInputNum).W)))
  val useconstpipe = Wire(Vec(srcnum,Bool()))
  val haveshiftconstpipe = Wire(Vec(srcnum,Bool()))
  val linkneedtosendoutpipe =Wire(Vec(crossbarOutputNum,Bool())) 
  val fuinstskippipe = Wire(Bool())
  val linkinstskippipe = Wire(Vec(crossbarOutputNum,Bool())) 
  val canexepipe = Wire(Bool())
  val Kpipe = Wire(UInt(PEctrlregsdWidth.W))
  val Jpipe = Wire(UInt(PEctrlregsdWidth.W))
  val Ipipe = Wire(UInt(PEctrlregsdWidth.W))
  val constpipe = Wire(Vec(constmemNum,UInt(constMemdWidth.W)))
  val shiftconstpipe = Wire(Vec(shiftconstmemNum,UInt(shiftconstMemdWidth.W)))
  val finishpipe = Wire(UInt(PEctrlregsdWidth.W))

  PipelineConnect(Decoder.io.alukey,alukeypipe,!io.run)
  PipelineConnect(Decoder.io.srckey,srckeypipe,!io.run)
  PipelineConnect(Decoder.io.linkkey,linkkeypipe,!io.run)
  PipelineConnect(Decoder.io.useconst,useconstpipe,!io.run)
  PipelineConnect(Decoder.io.haveshiftconst,haveshiftconstpipe,!io.run)
  PipelineConnect(Decoder.io.linkneedtosendout,linkneedtosendoutpipe,!io.run)
  PipelineConnect(Decoder.io.fuinstskip,fuinstskippipe,!io.run)
  PipelineConnect(Decoder.io.linkinstskip,linkinstskippipe,!io.run)
  PipelineConnect(Decoder.io.canexe,canexepipe,!io.run)
  PipelineConnect(regs(KIndex),Kpipe,!io.run)
  PipelineConnect(regs(JIndex),Jpipe,!io.run)
  PipelineConnect(regs(IIndex),Ipipe,!io.run)
  PipelineConnect(Constmems(0).io.rdata,constpipe(0),!io.run)
  PipelineConnect(Constmems(1).io.rdata,constpipe(1),!io.run)
  PipelineConnect(Shiftconstmems(0).io.rdata,shiftconstpipe(0),!io.run)
  PipelineConnect(Shiftconstmems(1).io.rdata,shiftconstpipe(1),!io.run)
  PipelineConnect(regs(FinishIndex),finishpipe,!io.run)



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

  def Loopnuminit(cntID:Int,incID:Int,threadID:Int,change:Bool): Bool = {
    Mux(regs(incID).asSInt>0.S,regs(cntID).asSInt + regs(incID).asSInt >= regs(threadID).asSInt,regs(cntID).asSInt + regs(incID).asSInt <= regs(threadID).asSInt)&change 
  }
  val Kchange = regs(InstcntIndex) === regs(InstnumIndex) -1.U
  val Kinit = Loopnuminit(KIndex,K_incIndex,K_threadIndex,Kchange)
  val Knew = Mux(Kinit,regs(K_initIndex),regs(KIndex) + regs(K_incIndex))
  ctrlregnextmap +=(KIndex->Knew)
  val Jchange = Kinit
  val Jinit = Loopnuminit(JIndex,J_incIndex,J_threadIndex,Jchange)
  val Jnew = Mux(Jinit,regs(J_initIndex),regs(JIndex) + regs(J_incIndex))
  ctrlregnextmap +=(JIndex->Jnew)
  val Ichange = Jinit
  val Iinit = Loopnuminit(IIndex,I_incIndex,I_threadIndex,Ichange)
  val Inew = Mux(Iinit,regs(I_initIndex),regs(IIndex) + regs(I_incIndex))
  ctrlregnextmap +=(IIndex->Inew)
  ctrlregnextmap +=(StartcyclecntIndex -> Mux(regs(StartcyclecntIndex) < regs(StartcyclenumIndex), regs(StartcyclecntIndex) + 1.U,regs(StartcyclecntIndex)))
  val regscanupdatestate =Decoder.io.canexe & io.run & (regs(FinishIndex) === 0.U)//TODO:if Crossbar have less then 4 outputs ?
  val canupdatestatepipe = canexepipe & io.run & (!finishpipe)
  ctrlregwenmap +=(InstcntIndex->regscanupdatestate)
  ctrlregwenmap +=(Constcnt1Index->(regscanupdatestate&Decoder.io.useconst(0)))
  ctrlregwenmap +=(Constcnt2Index->(regscanupdatestate&Decoder.io.useconst(1)))
  ctrlregwenmap +=(Shiftconstcnt1Index->(regscanupdatestate&Decoder.io.haveshiftconst(0)))
  ctrlregwenmap +=(Shiftconstcnt2Index-> (regscanupdatestate&Decoder.io.haveshiftconst(1)))
  ctrlregwenmap +=(IIcntIndex->regscanupdatestate)
  ctrlregwenmap +=(FinishIndex->regscanupdatestate)
  ctrlregwenmap +=(KIndex->(regscanupdatestate&Kchange))
  ctrlregwenmap +=(JIndex->(regscanupdatestate&Jchange))
  ctrlregwenmap +=(IIndex->(regscanupdatestate&Ichange))
  ctrlregwenmap +=(StartcyclecntIndex->io.run)

  //fureg
  Fureg.io.inData := Alu.io.result.bits
  Fureg.io.enable := canupdatestatepipe & Alu.io.result.valid

  //PEctrlregs
  PEctrlregs.io.configwen := io.wen
  PEctrlregs.io.configwaddr := io.waddr
  PEctrlregs.io.configwdata := io.wdata
  PEctrlregs.io.inData := DontCare
  PEctrlregs.io.wen := DontCare
  PEctrlregs.io.outData:=DontCare


  //Instmems
  Instmems.foreach(_.io.raddr:= Mux(regscanupdatestate,ctrlregnextmap(InstcntIndex),regs(InstcntIndex)))
  Instmems.zipWithIndex.foreach { case (instMem, i) =>
      instMem.io.waddr := io.waddr - (instMemSize * i + instMemStartaddr).asUInt()
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
Constmems(0).io.raddr:=Mux(regscanupdatestate&Decoder.io.useconst(0),ctrlregnextmap(Constcnt1Index),regs(Constcnt1Index))
Constmems(1).io.raddr:=Mux(regscanupdatestate&Decoder.io.useconst(1),ctrlregnextmap(Constcnt2Index),regs(Constcnt2Index))
Constmems.zipWithIndex.foreach { case (constMem, i) =>
      constMem.io.waddr := io.waddr - (constMemSize * i + constMemStartaddr).asUInt()
      constMem.io.wen := io.wen && io.waddr >= (constMemStartaddr+constMemSize*i).U && io.waddr < (constMemStartaddr + constMemSize*(i+1)).U
      constMem.io.wdata := io.wdata
  }

  //ShiftConstmems
  Shiftconstmems(0).io.raddr:= Mux(regscanupdatestate&Decoder.io.haveshiftconst(0),ctrlregnextmap(Shiftconstcnt1Index),regs(Shiftconstcnt1Index))
  Shiftconstmems(1).io.raddr:= Mux(regscanupdatestate&Decoder.io.haveshiftconst(1),ctrlregnextmap(Shiftconstcnt2Index),regs(Shiftconstcnt2Index))
  Shiftconstmems.zipWithIndex.foreach { case (shiftconstMem, i) =>
      shiftconstMem.io.waddr := io.waddr - (shiftconstMemSize * i + shiftconstMemStartaddr).asUInt()
      shiftconstMem.io.wen := io.wen && io.waddr >= (shiftconstMemStartaddr+shiftconstMemSize*i).U && io.waddr < (shiftconstMemStartaddr + shiftconstMemSize*(i+1)).U
      shiftconstMem.io.wdata := io.wdata
  }

  Srcmuxs.zipWithIndex.foreach {case(srcmux,i) => 
    srcmux.io.sel := srckeypipe(i)
    srcmux.io.in(0):= 0.U
    srcmux.io.in(1):= Fureg.io.outData
    srcmux.io.in(2):= constpipe(i)
    io.inLinks.zipWithIndex.foreach{case(link,j)=>srcmux.io.in(j+3):=link}
    srcmux.io.in(7):= Kpipe
    srcmux.io.in(8):= Jpipe
    srcmux.io.in(9):= Ipipe
  }
  //FU
  Alu.io.fn := alukeypipe
  Alu.io.src1:=Mux(haveshiftconstpipe(0),(Srcmuxs(0).io.out.asSInt +shiftconstpipe(0).asSInt).asUInt,Srcmuxs(0).io.out)
  Alu.io.src2:=Mux(haveshiftconstpipe(1),(Srcmuxs(1).io.out.asSInt +shiftconstpipe(1).asSInt).asUInt,Srcmuxs(1).io.out)
  
  //Crossbar
  Crossbar.io.select := linkkeypipe
  Crossbar.io.in(0):=0.U
  io.inLinks.zipWithIndex.foreach{case(link,i)=>Crossbar.io.in(i+1):=link}
  Crossbar.io.in(5):=Alu.io.result.bits
  Crossbar.io.in(6):=Fureg.io.outData

  io.outLinks.zipWithIndex.foreach{case(link,i) =>
    link.bits := Crossbar.io.out(i)
    link.valid := canupdatestatepipe & linkneedtosendoutpipe(i) & (!linkinstskippipe(i))
  }
      /*
  io.outLinks:=Crossbar.io.out.map(signal => {
    val updateout = Wire(UInt(crossbarDataWidth.W))
    updateout:= signal
    updateout
  })

  */
  //datamem
  io.datamemio <> Alu.io.datamemio
  io.datamemio.ren := Alu.io.datamemio.ren & canupdatestatepipe & (!fuinstskippipe)
  io.datamemio.wen := Alu.io.datamemio.wen & canupdatestatepipe & (!fuinstskippipe)

  io.finish :=  finishpipe(0).asBool
}
