/*
 *
 * An ALU is a minimal start for a processor.
 *
 */

package utils 

import chisel3._
import chisel3.util._

object AluOpcode {
  def apply() = Map(
    "nul"->  0.U,
    "add" -> 1.U,
    "sub" -> 2.U,
    "mul" -> 3.U,
    "div" -> 4.U
    )
}
object AluOperations{
  val nul: (SInt,SInt) => SInt = {(src1,src2) => 0.S}
  val add: (SInt,SInt) => SInt = {(src1,src2) => src1+src2}
  val sub: (SInt,SInt) => SInt = {(src1,src2) => src1-src2}
  val mul: (SInt,SInt) => SInt = {(src1,src2) => src1*src2}
  val div: (SInt,SInt) => SInt = {(src1,src2) => src1/src2}
  def apply() = Map(
    "nul" -> nul,
    "add" -> add,
    "sub" -> sub,
    "mul" -> mul,
    "div" -> div
    )
}
class Alu (dataWidth:Int,functionNum:Int,opneed:List[String])extends Module {
  val io = IO(new Bundle {
    val fn = Input(UInt(log2Ceil(functionNum).W))
    val src1 = Input(SInt(dataWidth.W))
    val src2 = Input(SInt(dataWidth.W))
    val result = Output(SInt(dataWidth.W))
  })

  // Use shorter variable names
  val fn = io.fn
  val src1 = io.src1
  val src2 = io.src2
  val result = Wire(SInt(dataWidth.W))
  val supportedOpcode = AluOpcode()
  val supportedOperation = AluOperations()
  
  //checkthe data
  opneed.map(op =>{
    if(supportedOpcode.contains(op) == false){
      println("some option is not include in AluOpcode")
    }
    if(supportedOperation.contains(op) == false){
      println("some option is not include in AluOperations")
    }
  })

  // some default value is needed
  result := 0.S
  opneed.map(op => {
    var opt : (SInt,SInt) => SInt = supportedOperation.get(op).get
    when (fn === supportedOpcode.getOrElse(op,0.U)){
      result := opt(src1,src2)
    }
  })
  io.result := result
}
class Aluvalid (dataWidth:Int,functionNum:Int,opneed:List[String])extends Module {
  val io = IO(new Bundle {
    val fn = Input(UInt(log2Ceil(functionNum).W))
    val src1 = Input(Valid(UInt(dataWidth.W)))
    val src2 = Input(Valid(UInt(dataWidth.W)))
    val result = Output(Valid(UInt(dataWidth.W)))
  })

  // Use shorter variable names
  val fn = io.fn
  val src1 = io.src1.bits.asSInt
  val src2 = io.src2.bits.asSInt
  val result = Wire(SInt(dataWidth.W))
  val supportedOpcode = AluOpcode()
  val supportedOperation = AluOperations()
  
  //checkthe data
  opneed.map(op =>{
    if(supportedOpcode.contains(op) == false){
      println("some option is not include in AluOpcode")
    }
    if(supportedOperation.contains(op) == false){
      println("some option is not include in AluOperations")
    }
  })
  // some default value is needed
  result := 0.S
  opneed.map(op => {
    var opt : (SInt,SInt) => SInt = supportedOperation.get(op).get
    when (fn === supportedOpcode.getOrElse(op,0.U)){
      result := opt(src1,src2)
    }
  })
  io.result.bits := result.asUInt
  io.result.valid := io.src1.valid & io.src2.valid
}
