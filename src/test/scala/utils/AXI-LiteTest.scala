package utils 

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
class AXILiteModuleTester extends AnyFlatSpec with ChiselScalatestTester{
  "AXILite test" should "pass" in {
    test(new AXI4LiteModule) { dut=>
def writereg(addr : Int, data:Int): Unit = {
dut.io.awaddr.bits.poke(addr.U)
dut.io.awaddr.valid.poke(true.B)
dut.io.wdata.bits.poke(data.U)
dut.io.wdata.valid.poke(true.B)
dut.io.wstrb.poke("b1111".U)
dut.io.bresp.ready.poke(true.B)
  dut.clock.step(1)
dut.io.awaddr.valid.poke(false.B)
  dut.clock.step(1)
dut.io.wdata.valid.poke(false.B)

dut.io.bresp.bits.expect(0.U)
dut.io.bresp.valid.expect(true.B)
}

def readreg(addr : Int, data:Int): Unit = {
dut.io.araddr.bits.poke(addr.U)
dut.io.araddr.valid.poke(true.B)
dut.clock.step(1)
dut.io.rdata.ready.poke(true.B)

dut.io.rdata.valid.expect(true.B)
dut.io.rdata.bits.expect(data.U)

dut.io.araddr.valid.poke(false.B)
dut.io.rdata.ready.poke(false.B)
}

writereg(4,123)
readreg(4,123)
readreg(4,123)
readreg(4,123)
    }
  }
}

