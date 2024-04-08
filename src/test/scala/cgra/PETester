package cgra

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Test the Mem,firstly test the SyncReadMem and then test Mem.
 * the data 0,1,2,3,4,5...is write into Mem by using all writeports parallelly, you can read the code in write method below
 * so the 2^width >=depth is needed
 * then we read data that has been written to memory,and check if they are the right value
 */
class PETester extends AnyFlatSpec with ChiselScalatestTester{
  val inst = "b00000000000000001000000000000111".U
  "PE test" should "pass" in {
    test(new PE(inputCount = 4,outputCount = 4)){ c =>
        for (i <-0 until 8){
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(inst)
        c.io.waddr.poke((i).U)
        c.clock.step()
        }
        for (i <-8 until 40){
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(0.U)
        c.io.waddr.poke((i).U)
        c.clock.step()
        }
        //constmem1
        for (i <-48 until 56){
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(0.U)
        c.io.waddr.poke((i).U)
        c.clock.step()
        }
        //constmem2
        for (i <-56 until 64){
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(i.U)
        c.io.waddr.poke((i).U)
        c.clock.step()
        }
        //shiftconstmem
        for (i <-64 until 80){
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(i.U)
        c.io.waddr.poke((i).U)
        c.clock.step()
        }
        //ctrlregs
        //Instnum
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(8.U)
        c.io.waddr.poke((80).U)
        c.clock.step()
        //Constnum1
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(8.U)
        c.io.waddr.poke((80+4).U)
        c.clock.step()
        //Constnum2
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(8.U)
        c.io.waddr.poke((80+5).U)
        c.clock.step()
        //ShiftConstnum1
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(8.U)
        c.io.waddr.poke((80+6).U)
        c.clock.step()
        //ShiftConstnum2
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(8.U)
        c.io.waddr.poke((80+7).U)
        c.clock.step()
        //Instcnt
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(0.U)
        c.io.waddr.poke((80+17).U)
        c.clock.step()
        //Constcnt1
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(0.U)
        c.io.waddr.poke((80+19).U)
        c.clock.step()
        //Constcnt2
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(0.U)
        c.io.waddr.poke((80+20).U)
        c.clock.step()
        //Shiftconstcnt1
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(0.U)
        c.io.waddr.poke((80+21).U)
        c.clock.step()
        //Shiftconstcnt2
        c.io.run.poke(false.B)
        c.io.wen.poke(true.B)
        c.io.wdata.poke(0.U)
        c.io.waddr.poke((80+22).U)
        c.clock.step()
        //delay
        c.clock.step()

        c.io.wen.poke(false.B)
        c.io.run.poke(true.B)
        for (i <-0 until 8){
                println(s"src1: ${c.io.src1key.peek().litValue()}")
                println(s"src2: ${c.io.src2key.peek().litValue()}")
                println(s"shiftc1: ${c.io.rdata(8).peek().litValue()}")
                println(s"shiftc2: ${c.io.rdata(9).peek().litValue()}")
                println(s"alu: ${c.io.alukey.peek().litValue()}")
                println(s"const1addr: ${c.io.const1raddr.peek().litValue()}")
                println(s"const2addr: ${c.io.const2raddr.peek().litValue()}")
                println(s"Instcnt: ${c.io.rdata(10).peek().litValue()}")
                println(s"mux1: ${c.io.rdata(11).peek().litValue()}")
                println(s"mux2: ${c.io.rdata(12).peek().litValue()}")
                println(s"mux1valid: ${c.io.mux1valid.peek().litValue()}")
                println(s"mux2valid: ${c.io.mux2valid.peek().litValue()}")
                println(s"useconst1: ${c.io.useconst1.peek().litValue()}")
                println(s"useconst2: ${c.io.useconst2.peek().litValue()}")
                println(s"furegvalue: ${c.io.aluresult.peek().litValue()}")
                c.clock.step()
        }
    }
  }
}
