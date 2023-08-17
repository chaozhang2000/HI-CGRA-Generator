/**************************************************************************************
* Copyright (c) 2020 Institute of Computing Technology, CAS
* Copyright (c) 2020 University of Chinese Academy of Sciences
* 
* NutShell is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2. 
* You may obtain a copy of Mulan PSL v2 at:
*             http://license.coscl.org.cn/MulanPSL2 
* 
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER 
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR 
* FIT FOR A PARTICULAR PURPOSE.  
*
* See the Mulan PSL v2 for more details.  
***************************************************************************************/

package utils

import chisel3._
import chisel3.util._

//this PipelineConnect is a function
//it has been test in nutshell,so we don't write a testbench for it, 
//but we write a Module to explain how it used,you can also generate the verilog using this module
object PipelineConnect {
  def apply[T <: Data](left: DecoupledIO[T], right: DecoupledIO[T], rightOutFire: Bool, isFlush: Bool) = {
    val valid = RegInit(false.B)
when(rightOutFire &&(~(left.valid&&right.ready))) {valid := false.B }
when(left.valid && right.ready) { valid := true.B}
when (isFlush) {valid := false.B}

    left.ready := right.ready
    right.bits := RegEnable(left.bits, left.valid && right.ready)
    right.valid := valid
  }
}

class PipelineConnectTest extends Module {
val io = IO(new Bundle {
val inLeft = Flipped(Decoupled(Output(UInt(1.W))))
val inRight = Decoupled(Output(UInt(1.W)))
val rightOutFire = Input(Bool())
val isFlush = Input(Bool())
})
PipelineConnect(io.inLeft, io.inRight, io.rightOutFire, io.isFlush)
}
