package utils 
import chisel3._
import chisel3.util._
class GenericMux(inWidth: Int, numOfInputs: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(numOfInputs, UInt(inWidth.W))) // 输入
    val sel = Input(UInt(log2Ceil(numOfInputs).W)) // 选择信号
    val out = Output(UInt(inWidth.W)) // 输出
  })

  io.out := MuxLookup(io.sel, 0.U, 
    (0 until numOfInputs).map(i => i.U -> io.in(i))) // 使用 MuxLookup 构建 Mux 逻辑
}
