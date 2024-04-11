package utils 
import chisel3._
import chisel3.util._

class AXI4StreamSlaveIO(Width:Int = 32) extends Bundle{ 
    val valid = Input(Bool())
    val ready = Output(Bool())
    val data = Input(UInt(Width.W))
    val last = Input(Bool())
}
