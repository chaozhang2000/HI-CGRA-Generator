package cgra
import chisel3._
import chisel3.util._

class CGRA extends Module with CGRAparams{
    val io = IO(new Bundle {
          val wen = Input(Bool())
          val waddr= Input(UInt(awidth.W))
          val wdata = Input(UInt(dwidth.W))
          val run = Input(Bool())
          val finish = Output(Bool())

          val axilite_s = new utils.AXI4LiteSlaveIO 
    })
    val PEs = Array.fill(cgrarows * cgracols)(Module(new PE))
    val Links = Array.fill(2 *cgrarows*(cgracols-1) + 2*cgracols * (cgrarows -1))(Module(new Link))
    val Datamem= Array.fill(cgrarows*cgracols)(Module(new Datamem))
    val ctrlregs = Reg(Vec(CGRActrlregsNum, UInt(CGRActrlregsdWidth.W)))

    // init PEs io.inLinks
    for( i<-0 until cgrarows * cgracols ) {
      (0 until pelinkNum ).foreach { linkindex => PEs(i).io.inLinks(linkindex):= 0.U}
          PEs(i).io.wen := io.wen
          PEs(i).io.waddr:=io.waddr
          PEs(i).io.wdata:=io.wdata
          PEs(i).io.run := io.run
    }
    //connect datamem
    (0 until cgrarows*cgracols).foreach {i => PEs(i).io.datamemio <> Datamem(i).io}


    //connect pes and links
    var linkid = 0
    for( r <- 0 until cgrarows){
      for ( c <- 0 until cgracols){
        if(r < cgrarows -1){//to N
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(0)
          PEs((r+1)*cgracols+c).io.inLinks(1) := Links(linkid).io.out
          linkid += 1
        }
        if(r > 0){//to S
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(1)
          PEs((r-1)*cgracols+c).io.inLinks(0) := Links(linkid).io.out
          linkid += 1
        }
        if(c < cgracols -1){//to E
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(3)
          PEs(r*cgracols+c+1).io.inLinks(2) := Links(linkid).io.out
          linkid += 1
        }
        if(c > 0){//to W
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(2)
          PEs(r*cgracols+c-1).io.inLinks(3) := Links(linkid).io.out
          linkid += 1
        }
      }
    }
    io.finish := PEs.map(_.io.finish).reduce(_ && _)
  //--------------------------------------------AXI-Lite-----------------------------------------------

  val statew = RegInit(0.U(2.W))
  val stater = RegInit(0.U(2.W))
  val currentAddressr = RegInit(0.U(32.W))
  val currentAddressw = RegInit(0.U(32.W))
  val ctrlregs_axil_wen = Wire(Bool())
  val ctrlregs_axil_wdata = Wire(UInt(CGRActrlregsdWidth.W))

  ctrlregs_axil_wen:= false.B
  ctrlregs_axil_wdata := 0.U

  io.axilite_s.bresp.valid := false.B
  io.axilite_s.bresp.bits := 0.U

  io.axilite_s.awaddr.ready := statew === 0.U
  io.axilite_s.wdata.ready := statew ===1.U

  val mask = Cat(Fill(8, io.axilite_s.wstrb(3)), Fill(8, io.axilite_s.wstrb(2)), Fill(8, io.axilite_s.wstrb(1)), Fill(8, io.axilite_s.wstrb(0)))
  switch(statew) {
    is(0.U) {
      when(io.axilite_s.awaddr.valid&& io.axilite_s.awaddr.ready){
        currentAddressw:= (io.axilite_s.awaddr.bits-cgrastartaddr.U)(31,2)
        statew := 1.U
      }
    }
    is(1.U) {
      when(io.axilite_s.wdata.valid&& io.axilite_s.wdata.ready) {
        ctrlregs_axil_wen := true.B
        ctrlregs_axil_wdata :=(ctrlregs(currentAddressw) & (~mask)) |(io.axilite_s.wdata.bits &mask)
        //ctrlregs(currentAddressw) := (ctrlregs(currentAddressw) & (~mask)) |(io.axilite_s.wdata.bits &mask)
        statew := 2.U
      }
    }
    is(2.U) {
      when(io.axilite_s.bresp.ready) {
        io.axilite_s.bresp.valid := true.B
        io.axilite_s.bresp.bits:= 0.U 
        statew := 0.U
      }
    }
  }

  io.axilite_s.araddr.ready := stater ===0.U
  io.axilite_s.rdata.valid:= stater === 1.U
  switch(stater) {
    is(0.U) {
      when(io.axilite_s.araddr.valid&& io.axilite_s.araddr.ready){
        currentAddressr:= (io.axilite_s.araddr.bits-cgrastartaddr.U)(31,2)
        stater := 1.U
      }
    }
    is(1.U) {
      when(io.axilite_s.rdata.valid&& io.axilite_s.rdata.ready) {
        stater := 0.U
      }
    }
  }
  io.axilite_s.rdata.bits := Mux((stater === 1.U)&&io.axilite_s.rdata.valid && io.axilite_s.rdata.ready,ctrlregs(currentAddressr),0.U)

  io.axilite_s.rresp := 0.U

  when(ctrlregs_axil_wen){
    ctrlregs(currentAddressw) := ctrlregs_axil_wdata
  }
  //--------------------------------------------state machine----------------------------------------------------

}
