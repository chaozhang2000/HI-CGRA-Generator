package cgra 

import chisel3._
import chisel3.util._

//TODO: result_valid
//TODO: shl
object AluOpcode {
  def apply() = Map(
    "nul" -> 0.U,
    "mul" -> 1.U,
    "add" -> 2.U,
    "getelementptr" -> 3.U,
    "load" -> 4.U,
    "store" -> 5.U,
    "shl" -> 6.U,
    "or"  -> 7.U,
    "sext" -> 8.U,
    "and" -> 9.U,
    "sub" -> 10.U,
    "ashr" -> 11.U
    )
}
object AluOperations{
  val add: (SInt,SInt) => SInt = {(src1,src2) => src1+src2}
  val mul: (SInt,SInt) => SInt = {(src1,src2) => src1*src2}
  val or: (SInt,SInt) => SInt = {(src1,src2) => src1|src2}
  val sext: (SInt,SInt) => SInt = {(src1,src2) => src1}
  val and: (SInt,SInt) => SInt = {(src1,src2) => src1&src2}
  val sub: (SInt,SInt) => SInt = {(src1,src2) => src1-src2}
  def apply() = Map(
    "mul" -> mul,
    "add" -> add,
    "getelementptr" -> add,
    "or"  -> or,
    "sext" -> sext,
    "and" -> and,
    "sub" -> sub
    )
}
class Fu (ID:Int) extends Module with CGRAparams{
  val io = IO(new Bundle {
    val fn = Input(UInt(log2Ceil(aluoptnum).W))
    val src1 = Input(UInt(aluwidth.W))
    val src2 = Input(UInt(aluwidth.W))
    val result = Output(Valid(UInt(aluwidth.W)))

    val datamemio = Flipped(new DataMemIO)
  })

  io.datamemio.wen := false.B
  io.datamemio.ren := false.B
  io.datamemio.raddr:= 0.U
  io.datamemio.waddr:= 0.U
  io.datamemio.wdata:= 0.U

  // Use shorter variable names
  val fn = io.fn
  val src1 = io.src1.asSInt
  val src2 = io.src2.asSInt
  val result= Wire(Vec(aluoptnum,UInt(aluwidth.W)))
  val result_valid = Wire(Vec(aluoptnum,Bool()))
  (0 until aluoptnum).foreach{ i => result(i):= 0.U;result_valid(i) := false.B}
  val supportedOpcode = AluOpcode()
  val supportedOperation = AluOperations()
  val outmux = Module(new utils.Muxonehot(dWidth=aluwidth,numOfInputs=aluoptnum))
  //checkthe data
  aluoptlist.map(op =>{
    if(supportedOpcode.contains(op) == false){
      println("some option is not include in AluOpcode")
    }
  })
  val opresultmap = aluoptlist.map(op=> op->result(aluoptlist.indexOf(op))).toMap
  val opresultvalidmap = aluoptlist.map(op=> op->result_valid(aluoptlist.indexOf(op))).toMap

  // some default value is needed
  aluoptlist.map(op => {
    if(supportedOperation.contains(op) == true){
    var opt : (SInt,SInt) => SInt = supportedOperation.get(op).get
    when (fn === supportedOpcode.getOrElse(op,0.U)){
      if(op == "mul" && use16bitmul){
      opresultmap(op) := opt(src1(15,0).asSInt,src2(15,0).asSInt).asUInt
      }else{
      opresultmap(op) := opt(src1,src2).asUInt
      }
      opresultvalidmap(op) :=true.B 
    }
    }
  })
  //nul
  if(aluoptlist.contains("nul")){
    when (fn === supportedOpcode("nul")){
      opresultmap("nul") := 0.U
      opresultvalidmap("nul") := false.B
    }
  }
  //shl
  if(aluoptlist.contains("shl")){
    when (fn === supportedOpcode("shl")){
      opresultmap("shl") := (src1 << src2(4,0)).asUInt
      opresultvalidmap("shl") := true.B
    }
  }
  //ashr
  if(aluoptlist.contains("ashr")){
    when (fn === supportedOpcode("ashr")){
      opresultmap("ashr") := (src1.asSInt() >> src2(4,0)).asUInt
      opresultvalidmap("ashr") := true.B
    }
  }
  //load
  if(aluoptlist.contains("load")){
    when (fn === supportedOpcode("load")){
      io.datamemio.raddr:= io.src1
      io.datamemio.ren := true.B
    }
      opresultmap("load") := io.datamemio.rdata
      opresultvalidmap("load") := io.datamemio.memoptvalid && (io.datamemio.peidfm === ID.U)
  }
  //store
  if(aluoptlist.contains("store")){
    when (fn === supportedOpcode("store")){
      io.datamemio.waddr:= io.src2
      io.datamemio.wdata:= io.src1
      io.datamemio.wen := true.B
    }
      opresultvalidmap("store") := io.datamemio.memoptvalid && (io.datamemio.peidfm === ID.U)
  }
  outmux.io.sel := result_valid
  outmux.io.in := result
  io.result.bits := outmux.io.out
  io.result.valid := result_valid.reduce(_|_)

  io.datamemio.peid2m := ID.U
}
