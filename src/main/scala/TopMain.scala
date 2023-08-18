package top
import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation,ChiselStage}
import chisel3.tester._
import chisel3.tester.RawTester.test
object TopMain extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(()=> new utils.Crossbar(5,6,8)))
    )
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(()=> new utils.PipelineConnectTest()))
    )
  val list = List("add","sub","mul","div")
  val width = 32
  val num = 3 
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(()=> new utils.Alu(width,num,list)))
    )
  //(new ChiselStage).execute(args,
  //  Seq(ChiselGeneratorAnnotation(()=> new Tile()))
  //  )
}
