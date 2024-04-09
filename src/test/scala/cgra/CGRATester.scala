package cgra

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
class CGRATester extends AnyFlatSpec with ChiselScalatestTester{
  "CGRA test" should "pass" in {
    test(new CGRA) { dut=>
def writereg(addr : Int, data:Int): Unit = {
dut.io.axilite_s.awaddr.bits.poke(addr.U)
dut.io.axilite_s.awaddr.valid.poke(true.B)
dut.io.axilite_s.wdata.bits.poke(data.U)
dut.io.axilite_s.wdata.valid.poke(true.B)
dut.io.axilite_s.wstrb.poke("b1111".U)
dut.io.axilite_s.bresp.ready.poke(true.B)
  dut.clock.step(1)
dut.io.axilite_s.awaddr.valid.poke(false.B)
  dut.clock.step(1)
dut.io.axilite_s.wdata.valid.poke(false.B)

dut.io.axilite_s.bresp.bits.expect(0.U)
dut.io.axilite_s.bresp.valid.expect(true.B)
}

def readreg(addr : Int, data:Int): Unit = {
dut.io.axilite_s.araddr.bits.poke(addr.U)
dut.io.axilite_s.araddr.valid.poke(true.B)
dut.clock.step(1)
dut.io.axilite_s.rdata.ready.poke(true.B)

dut.io.axilite_s.rdata.valid.expect(true.B)
dut.io.axilite_s.rdata.bits.expect(data.U)

dut.io.axilite_s.araddr.valid.poke(false.B)
dut.io.axilite_s.rdata.ready.poke(false.B)
}

writereg(4,123)
readreg(4,123)
readreg(4,123)
readreg(4,123)
    }
  }
}

