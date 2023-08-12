package top
import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation,ChiselStage}
import chisel3.tester._
object TopMain extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(()=> new Crossbar(5,6,8)))
    )
}
