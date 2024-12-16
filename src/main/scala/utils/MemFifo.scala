package utils
import chisel3._
import chisel3.util._
class WriterIO(size: Int) extends Bundle {
    val write = Input(Bool())
    val full = Output(Bool())
    val din = Input(UInt(size.W))
}
class ReaderIO(size: Int) extends Bundle {
    val read = Input(Bool())
    val empty = Output(Bool())
    val dout = Output(UInt(size.W))
}
abstract class Fifo(size: Int, val depth: Int) extends Module {
    val io = IO(new Bundle {
          val write = new WriterIO(size)
          val read = new ReaderIO(size)
    })
    assert(depth > 0, "Number of buffer elements needs to be larger than 0.")
}
class MemFifo(size: Int, depth: Int) extends Fifo(size: Int, depth: Int) {
    def counter(depth: Int, incr: Bool): (UInt, UInt) = {
        val cntReg = RegInit(0.U(log2Ceil(depth).W))
        val nextVal = Mux(cntReg === (depth-1).U, 0.U, cntReg + 1.U)
        when (incr) {
            cntReg := nextVal
        }
        (cntReg, nextVal)
    }

    val mem = SyncReadMem(depth, UInt(size.W))

    val incrRead = WireDefault(false.B)
    val incrWrite = WireDefault(false.B)
    val (readPtr, nextRead) = counter(depth, incrRead)
    val (writePtr, nextWrite) = counter(depth, incrWrite)

    val emptyReg = RegInit(true.B)
    val fullReg = RegInit(false.B)

    val idle :: valid :: full :: Nil = Enum(3)
    val stateReg = RegInit(idle)
    val shadowReg = Reg(UInt(size.W))

    // 写FIFO的处理是一样的
    when (io.write.write && !fullReg) {
        mem.write(writePtr, io.write.din)
        emptyReg := false.B
        fullReg := nextWrite === readPtr
        incrWrite := true.B
    }

    // 读基于内存的FIFO时要处理一个时钟周期的延迟
    val data = mem.read(readPtr)

    switch(stateReg) {
        is(idle) {
            when(!emptyReg) {
                stateReg := valid
                fullReg := false.B
                emptyReg := nextRead === writePtr
                incrRead := true.B
            }
        }
        is(valid) {
            when(io.read.read) {
                when(!emptyReg) {
                    stateReg := valid
                    fullReg := false.B
                    emptyReg := nextRead === writePtr
                    incrRead := true.B
                }.otherwise {
                    stateReg := idle
                }
            }.otherwise {
                shadowReg := data
                stateReg := full
            }
        }
        is(full) {
            when(io.read.read) {
                when(!emptyReg) {
                    stateReg := valid
                    fullReg := false.B
                    emptyReg := nextRead === writePtr
                    incrRead := true.B
                }.otherwise {
                    stateReg := idle
                }
            }
        }
    }

    io.read.dout := Mux(stateReg === valid, data, shadowReg)
    io.write.full := fullReg
    io.read.empty := emptyReg
}
