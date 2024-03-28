package cgra 

import chisel3._
import chisel3.util._

//TODO: result_valid
//TODO: shl
object AluOpcode {
  def apply() = Map(
    "mul" -> 1.U,
    "add" -> 2.U,
    "getelementptr" -> 3.U,
   // "load" -> 4.U,
    //"store" -> 5.U,
    "shl" -> 6.U
    )
}
object AluOperations{
  val add: (SInt,SInt) => SInt = {(src1,src2) => src1+src2}
  val sub: (SInt,SInt) => SInt = {(src1,src2) => src1-src2}
  val mul: (SInt,SInt) => SInt = {(src1,src2) => src1*src2}
  val div: (SInt,SInt) => SInt = {(src1,src2) => src1/src2}
  def apply() = Map(
    "mul" -> mul,
    "add" -> add,
    "getelementptr" -> add
    )
}
class Fu (dataWidth:Int,functionNum:Int,opneed:List[String])extends Module {
  val io = IO(new Bundle {
    val fn = Input(UInt(log2Ceil(functionNum).W))
    val src1 = Input(UInt(dataWidth.W))
    val src2 = Input(UInt(dataWidth.W))
    val result = Output(Valid(UInt(dataWidth.W)))
    //val result = Output(SInt(dataWidth.W))
  })

  // Use shorter variable names
  val fn = io.fn
  val src1 = io.src1.asSInt
  val src2 = io.src2.asSInt
  val result = Wire(SInt(dataWidth.W))
  val result_valid = Wire(Bool())
  val supportedOpcode = AluOpcode()
  val supportedOperation = AluOperations()
  
  //checkthe data
  opneed.map(op =>{
    if(supportedOpcode.contains(op) == false){
      println("some option is not include in AluOpcode")
    }
  })

  // some default value is needed
  result := 0.S
  result_valid := false.B
  opneed.map(op => {
    if(supportedOperation.contains(op) == true){
    var opt : (SInt,SInt) => SInt = supportedOperation.get(op).get
    when (fn === supportedOpcode.getOrElse(op,0.U)){
      result := opt(src1,src2)
      result_valid := true.B
    }
    }
  })
  //shl
  if(opneed.contains("shl")){
    when (fn === supportedOpcode("shl")){
      result:= src1 << src2(4,0)
      result_valid := true.B
    }
  }
  io.result.bits := result.asUInt
  io.result.valid := result_valid
}
