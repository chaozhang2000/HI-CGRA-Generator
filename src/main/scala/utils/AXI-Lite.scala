
package utils 
import chisel3._
import chisel3.util._
class AXI4LiteSlaveIO extends Bundle{ 
    val araddr = Flipped(Decoupled(UInt(32.W)))
    val rdata =Decoupled(UInt(32.W))
    val rresp = Output(UInt(2.W))

    val awaddr = Flipped(Decoupled(UInt(32.W)))
    val wdata = Flipped(Decoupled(UInt(32.W)))
    val wstrb = Input(UInt(4.W))
    val bresp = Decoupled(UInt(2.W))
}

class AXI4LiteModule(REG_COUNT: Int = 16) extends Module {
  val io = IO(new AXI4LiteSlaveIO)

  val registers = Reg(Vec(REG_COUNT, UInt(32.W)))

  val statew = RegInit(0.U(2.W))
  val stater = RegInit(0.U(2.W))
  val currentAddressr = RegInit(0.U(32.W))
  val currentAddressw = RegInit(0.U(32.W))

  io.bresp.valid := false.B
  io.bresp.bits := 0.U

  io.awaddr.ready := statew === 0.U
  io.wdata.ready := statew ===1.U

  val mask = Cat(Fill(8, io.wstrb(3)), Fill(8, io.wstrb(2)), Fill(8, io.wstrb(1)), Fill(8, io.wstrb(0)))
  switch(statew) {
    is(0.U) {
      when(io.awaddr.valid&& io.awaddr.ready){
        currentAddressw:= io.awaddr.bits(31,2)
        statew := 1.U
      }
    }
    is(1.U) {
      when(io.wdata.valid&& io.wdata.ready) {
        registers(currentAddressw) := (registers(currentAddressw) & (~mask)) |(io.wdata.bits &mask)
        statew := 2.U
      }
    }
    is(2.U) {
      when(io.bresp.ready) {
        io.bresp.valid := true.B
        io.bresp.bits:= 0.U 
        statew := 0.U
      }
    }
  }

  io.araddr.ready := stater ===0.U
  io.rdata.valid:= stater === 1.U
  switch(stater) {
    is(0.U) {
      when(io.araddr.valid&& io.araddr.ready){
        currentAddressr:= io.araddr.bits(31,2)
        stater := 1.U
      }
    }
    is(1.U) {
      when(io.rdata.valid&& io.rdata.ready) {
        stater := 0.U
      }
    }
  }
  io.rdata.bits := Mux((stater === 1.U)&&io.rdata.valid && io.rdata.ready,registers(currentAddressr),0.U)

  io.rresp := 0.U
}

