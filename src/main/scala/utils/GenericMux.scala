package utils 
import chisel3._
import chisel3.util._
class GenericMux(dWidth: Int, numOfInputs: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(numOfInputs, UInt(dWidth.W))) // 输入
    val sel = Input(UInt(log2Ceil(numOfInputs).W)) // 选择信号
    val out = Output(UInt(dWidth.W)) // 输出
  })

  /*
  val muxResult = MuxCase(0.U,(0 until numOfInputs).map { i =>
    (io.sel === i.U) -> io.in(i)
  })
  io.out := muxResult
  */
  io.out := MuxLookup(io.sel, 0.U, 
    (0 until numOfInputs).map(i => i.U -> io.in(i))) // 使用 MuxLookup 构建 Mux 逻辑
}
class Muxvalid(dWidth: Int, numOfInputs: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(numOfInputs, Valid(UInt(dWidth.W)))) // 输入
    val sel = Input(UInt(log2Ceil(numOfInputs).W)) // 选择信号
    val out = Output(Valid(UInt(dWidth.W))) // 输出
  })

  val defaultValue = Wire(Valid(UInt(dWidth.W)))
  defaultValue.bits := 0.U
  defaultValue.valid := false.B
  io.out := MuxLookup(io.sel,defaultValue, 
    (0 until numOfInputs).map(i => i.U -> io.in(i))) // 使用 MuxLookup 构建 Mux 逻辑
}

class Muxonehot(dWidth:Int,numOfInputs:Int) extends Module{
  val io = IO(new Bundle {
    val in = Input(Vec(numOfInputs, UInt(dWidth.W))) // 输入
    val sel = Input(Vec(numOfInputs,Bool())) // 选择信号
    val out = Output(UInt(dWidth.W)) // 输出
  })
  io.out := PriorityMux(io.in.zipWithIndex.map { case (input, idx) => (io.sel(idx) === true.B) -> input })
  //io.out := Mux1H(io.in.zipWithIndex.map { case (input, idx) => io.sel(idx) -> input })
}
