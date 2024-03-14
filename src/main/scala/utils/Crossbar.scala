package utils 
import chisel3._
import chisel3.util._
class Crossbar(inputNum: Int, outputNum: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(inputNum, UInt(dataWidth.W)))
    val select = Input(Vec(outputNum, UInt(log2Ceil(inputNum).W)))
    val out = Output(Vec(outputNum, UInt(dataWidth.W)))
  }) 

  /*first implement*/
 for (i <-0 until outputNum){
   io.out(i) := MuxLookup(io.select(i),0.U,(0 until inputNum).map(j=>j.U->io.in(j)))
 }
}
class Crossbarvalid(inputNum: Int, outputNum: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(inputNum, Valid(UInt(dataWidth.W))))
    val select = Input(Vec(outputNum, UInt(log2Ceil(inputNum).W)))
    val out = Output(Vec(outputNum, Valid(UInt(dataWidth.W))))
  }) 

  val defaultValue = Wire(Valid(UInt(dataWidth.W)))
  defaultValue.bits := 0.U
  defaultValue.valid := false.B
 for (i <-0 until outputNum){
   io.out(i) := MuxLookup(io.select(i),defaultValue,(0 until inputNum).map(j=>j.U->io.in(j)))
 }
}
