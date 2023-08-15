package top
import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation,ChiselStage}
import chisel3.tester._
import chisel3.tester.RawTester.test

object TopMain extends App {
  val vec = for(i<- 1 to 5 ) yield i
  val vec2 = Vec(2,UInt(2.W))
  println(vec2)
  println(vec)
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(()=> new Crossbar(5,6,8)))
    )
}
