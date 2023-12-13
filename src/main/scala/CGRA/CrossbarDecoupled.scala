package top 
import chisel3._
import chisel3.util._
class CrossbarDecouple(inputNum: Int, outputNum: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val in = Vec(inputNum, Flipped(Decoupled(Output(UInt(dataWidth.W)))))
    val select = Input(Vec(outputNum, UInt(log2Ceil(inputNum+1).W)))
    val out = Vec(outputNum, Decoupled(Output(UInt(dataWidth.W))))
  })
 var dataseq : Seq[(UInt,UInt)] = Seq()
 var validseq : Seq[(UInt,Bool)] = Seq()
 for (i<-0 until inputNum){
  dataseq = dataseq :+ ((i+1).U,io.in(i).bits)
  validseq = validseq :+ ((i+1).U,io.in(i).valid)
 }
 for (i <-0 until outputNum){
  io.out(i).bits := MuxLookup(io.select(i),0.U,dataseq)
  io.out(i).valid:= MuxLookup(io.select(i),false.B,validseq)
 }

// var readyseq : Seq[(UInt,Bool)] = Seq()
// for (i <-0 until outputNum){
//  readyseq = readyseq :+ 
// }


//  val muxOutputs = for (i <- 0 until outputNum) yield {
//    val muxInputs = for (j <- 0 until inputNum) yield {
//      Mux(io.select(i) === (j+1).U, io.in(j).bits, 0.U)
//    }
//    muxInputs.reduce(_ +& _)
//  }
//  val muxOutputsvalid = for (i <- 0 until outputNum) yield {
//    val muxInputsvalid = for (j <- 0 until inputNum) yield {
//      Mux(io.select(i) === (j+1).U, io.in(j).valid, 0.U)
//    }
//    muxInputsvalid.reduce(_ +& _)
//  }
//
  val muxInputsready = for (i <-0 until inputNum) yield {
    val muxOutputsready = for (j <-0 until outputNum) yield {
      Mux(io.select(j) === (i+1).U, io.out(j).ready,0.U)
    }
    muxOutputsready.reduce(_ +& _)
  }

//  for(i<-0 until outputNum){
//    io.out(i).bits := muxOutputs(i)
//    io.out(i).valid := muxOutputsvalid(i)
//  }

  for(i<-0 until inputNum){io.in(i).ready := muxInputsready(i)}
}
