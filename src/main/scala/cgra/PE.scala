package cgra
import chisel3._
import chisel3.util._

class PE(inputCount:Int,outputCount:Int) extends Module with CGRAparams{
    val io = IO(new Bundle {
          val inLinks = Input(Vec(inputCount,Valid(SInt(dwidth.W))))
          val outLinks = Output(Vec(inputCount,Valid(SInt(dwidth.W))))
          val wen = Input(Bool())
          val waddr= Input(UInt(awidth.W))
          val wdata = Input(UInt(dwidth.W))
          val rdata = Output(Vec(6+2,UInt(dwidth.W)))
  //decode out IO test
  val alukey= Output(UInt(log2Ceil(aluoptnum).W))
  val src1key = Output(UInt(log2Ceil(srcmuxInputNum).W))
  val src2key = Output(UInt(log2Ceil(srcmuxInputNum).W))
  val haveshiftconst1 = Output(Bool())
  val haveshiftconst2 = Output(Bool())
  val linkkey = Output(Vec(pelinkNum,UInt(log2Ceil(crossbarInputNum).W)))
          
    })
  //Instcntreg
  val Instcntreg = Module(new utils.Register(dwidth,0.S))
  Instcntreg.io.enable := true.B
  Instcntreg.io.inData := Mux(Instcntreg.io.outData<7.S,Instcntreg.io.outData + 1.S,0.S)//TODO

  //Instmems
  val Instmems = Seq.tabulate(instmemNum) { i =>
    Module(new utils.Memutil(depth = instMemSize,dwidth = instMemdWidth,awidth = instMemaWidth))
  }
  Instmems.foreach(_.io.raddr:= Instcntreg.io.inData.asUInt())
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
  Constmems.foreach(_.io.raddr:= Instcntreg.io.inData.asUInt())//TODO: should not be Instcntreg
  Constmems.zipWithIndex.foreach { case (constMem, i) =>
      constMem.io.waddr := io.waddr - (constMemSize * i).asUInt()
      constMem.io.wen := io.wen && io.waddr >= (constMemStartaddr+constMemSize*i).U && io.waddr < (constMemStartaddr + constMemSize*(i+1)).U
      constMem.io.wdata := io.wdata
  }
  io.rdata(6) := Constmems(0).io.rdata
  io.rdata(7) := Constmems(1).io.rdata

  //PEDecode
  val Decoder = Module(new PEDecode)
  Decoder.io.inst := Instmems(0).io.rdata
  io.alukey:=Decoder.io.alukey
  io.src1key := Decoder.io.src1key
  io.src2key := Decoder.io.src2key
  io.haveshiftconst1 := Decoder.io.haveshiftconst1
  io.haveshiftconst2 := Decoder.io.haveshiftconst2
  io.linkkey := Decoder.io.linkkey
  
  //Crossbar
  io.outLinks := io.inLinks
}
