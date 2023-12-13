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
 var dataseq : Seq[(UInt,UInt)] = Seq()
 for (i<-0 until inputNum){
  dataseq = dataseq :+ (i.U,io.in(i))
 }
 for (i <-0 until outputNum){
  io.out(i) := MuxLookup(io.select(i),0.U,dataseq)
 }
 /*second implement*/
//  val muxOutputs = for (i <- 0 until outputNum) yield {
//    val muxInputs = for (j <- 0 until inputNum) yield {
//      Mux(io.select(i) === j.U, io.in(j), 0.U)
//    }
//    muxInputs.reduce(_ +& _)
//  }
//
//  io.out := muxOutputs

}
