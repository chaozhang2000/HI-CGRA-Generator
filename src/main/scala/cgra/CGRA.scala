package cgra
import chisel3._
import chisel3.util._
import scala.collection.mutable.Map

class CGRA extends Module with CGRAparams{
    val io = IO(new Bundle {
          val finish = Output(Bool())

          val axilite_s = new utils.AXI4LiteSlaveIO 
          val axistream_s = new utils.AXI4StreamSlaveIO
          val axistream_m = Flipped(new utils.AXI4StreamSlaveIO)
    })
    //val PEs = Array.fill(cgrarows * cgracols)(Module(new PE(_)))
    val PEs = Array.tabulate(cgrarows * cgracols)(i => Module(new PE(i)))
    val Links = Array.fill(2 *cgrarows*(cgracols-1) + 2*cgracols * (cgrarows -1))(Module(new Link))
    val Datamem= Array.fill(datamemNum)(Module(new Datamem))

    val ctrlregs = RegInit(VecInit(Seq.fill(CGRActrlregsNum)(0.U(CGRActrlregsdWidth.W))))
    var ctrlregnextmap = Map.empty[Int,UInt]
    var ctrlregwenmap = Map.empty[Int,Bool]

    val configwaddr = RegInit(0.U(dwidth.W))
    val configPEcnt = RegInit(0.U(dwidth.W))
    val configwdata = Wire(UInt(dwidth.W))
    val configwen = Wire(Bool())
    configwdata := 0.U;configwen:=false.B

    val state = Map(
      "idle" -> 0.U,
      "config" -> 1.U,
      "loaddata" -> 2.U,
      "exe" -> 3.U,
      "getresult" -> 4.U
    )

    // init PEs io.inLinks
    for( i<-0 until cgrarows * cgracols ) {
      (0 until pelinkNum ).foreach { linkindex => PEs(i).io.inLinks(linkindex):= 0.U}
          PEs(i).io.wen := configwen &&(configPEcnt === i.U)
          PEs(i).io.waddr:=configwaddr
          PEs(i).io.wdata:=configwdata
          PEs(i).io.run := ctrlregs(CGRAstateIndex) === state("exe")
    }
    //connect datamem
    (0 until cgrarows*cgracols).foreach {i => PEs(i).io.datamemio.memoptvalid := 0.U;PEs(i).io.datamemio.rdata := 0.U;PEs(i).io.datamemio.peidfm := 0.U}
    /*
    (0 until datamemNum).foreach {i => 
      PEs(i).io.datamemio <> Datamem(i).io
      Datamem(i).io.wen := Mux((ctrlregs(CGRAstateIndex) === state("loaddata"))&&io.axistream_s.valid && io.axistream_s.ready,ctrlregs(CGRAdatamemIndex)===i.U,PEs(i).io.datamemio.wen)
      Datamem(i).io.waddr :=Mux((ctrlregs(CGRAstateIndex) === state("loaddata")),ctrlregs(CGRAdatamemstartaddrIndex) + ctrlregs(CGRAdatamemaddaddrIndex),PEs(i).io.datamemio.waddr)
      Datamem(i).io.wdata :=Mux((ctrlregs(CGRAstateIndex) === state("loaddata")),io.axistream_s.data,PEs(i).io.datamemio.wdata)

      Datamem(i).io.raddr := Mux(ctrlregs(CGRAstateIndex) === state("getresult"),ctrlregs(CGRAdatamemstartaddrIndex) + ctrlregs(CGRAdatamemaddaddrIndex) + Mux(io.axistream_m.valid && io.axistream_m.ready,1.U,0.U),PEs(i).io.datamemio.raddr)
      Datamem(i).io.ren := Mux(ctrlregs(CGRAstateIndex) === state("getresult"),true.B,PEs(i).io.datamemio.ren)
    }
    */
    (0 until datamemNum).foreach {i => 
      datamemaccess(i).foreach{ peid =>
        PEs(peid).io.datamemio.rdata := Datamem(i).io.rdata
        PEs(peid).io.datamemio.peidfm := Datamem(i).io.peidfm
        PEs(peid).io.datamemio.memoptvalid := Datamem(i).io.memoptvalid
      }
      
      val memwen = Wire(Bool())
      memwen := datamemaccess(i).map{peid => PEs(peid).io.datamemio.wen}.reduce(_ | _)
      val memren = Wire(Bool())
      memren := datamemaccess(i).map{peid => PEs(peid).io.datamemio.ren}.reduce(_ | _)
      val memwaddr = Wire(UInt(dataMemaWidth.W))
      memwaddr := PriorityMux(datamemaccess(i).map{peid => (PEs(peid).io.datamemio.wen -> PEs(peid).io.datamemio.waddr)})
      val memwdata = Wire(UInt(dataMemdWidth.W))
      memwdata := PriorityMux(datamemaccess(i).map{peid => (PEs(peid).io.datamemio.wen -> PEs(peid).io.datamemio.wdata)})
      val memraddr = Wire(UInt(dataMemaWidth.W))
      memraddr := PriorityMux(datamemaccess(i).map{peid => (PEs(peid).io.datamemio.ren -> PEs(peid).io.datamemio.raddr)})
      val peid2m= Wire(UInt(log2Ceil(cgrarows*cgracols).W))
      peid2m := PriorityMux(datamemaccess(i).map{peid => (PEs(peid).io.datamemio.ren|PEs(peid).io.datamemio.wen) -> PEs(peid).io.datamemio.peid2m})
      Datamem(i).io.wen := Mux((ctrlregs(CGRAstateIndex) === state("loaddata"))&&io.axistream_s.valid && io.axistream_s.ready,ctrlregs(CGRAdatamemIndex)===i.U,memwen)
      Datamem(i).io.waddr :=Mux((ctrlregs(CGRAstateIndex) === state("loaddata")),ctrlregs(CGRAdatamemstartaddrIndex) + ctrlregs(CGRAdatamemaddaddrIndex),memwaddr)
      Datamem(i).io.wdata :=Mux((ctrlregs(CGRAstateIndex) === state("loaddata")),io.axistream_s.data,memwdata)

      Datamem(i).io.raddr := Mux(ctrlregs(CGRAstateIndex) === state("getresult"),ctrlregs(CGRAdatamemstartaddrIndex) + ctrlregs(CGRAdatamemaddaddrIndex) + Mux(io.axistream_m.valid && io.axistream_m.ready,1.U,0.U),memraddr)
      Datamem(i).io.ren := Mux(ctrlregs(CGRAstateIndex) === state("getresult"),true.B,memren)

      Datamem(i).io.ren := Mux(ctrlregs(CGRAstateIndex) === state("getresult"),true.B,memren)

      Datamem(i).io.peid2m := peid2m
    }


    //connect pes and links
    var linkid = 0
    for( r <- 0 until cgrarows){
      for ( c <- 0 until cgracols){
        if(r < cgrarows -1){//to N
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(0)
          PEs((r+1)*cgracols+c).io.inLinks(0) := Links(linkid).io.out
          linkid += 1
        }
        if(r > 0){//to S
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(1)
          PEs((r-1)*cgracols+c).io.inLinks(1) := Links(linkid).io.out
          linkid += 1
        }
        if(c < cgracols -1){//to E
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(3)
          PEs(r*cgracols+c+1).io.inLinks(3) := Links(linkid).io.out
          linkid += 1
        }
        if(c > 0){//to W
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(2)
          PEs(r*cgracols+c-1).io.inLinks(2) := Links(linkid).io.out
          linkid += 1
        }
      }
    }
    val cgrafinish = Wire(Bool())
    cgrafinish := PEs.map(_.io.finish).reduce(_ && _)

    io.finish:= ctrlregs(CGRAfinishIndex)
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

  io.axilite_s.awaddr.ready := (statew === 0.U) 
  io.axilite_s.wdata.ready := (statew ===1.U)

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

  io.axilite_s.araddr.ready := (stater ===0.U)
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

  //--------------------------------------------state machine----------------------------------------------------
  val config_finish = Wire(Bool())
  config_finish := false.B

  val statenext = Wire(UInt(CGRActrlregsdWidth.W))
  statenext :=ctrlregs(CGRAstateIndex)

  when((ctrlregs(CGRAstateIndex) === state("config") && config_finish === true.B)|(ctrlregs(CGRAstateIndex) === state("exe") && cgrafinish === true.B)){
    statenext := 0.U
  }
  //config state
  val configPEnext = Wire(UInt(dwidth.W))
  val configwaddrnext = Wire(UInt(dwidth.W))
  configwaddrnext := Mux((configwaddr< peendaddr.U),configwaddr+ 1.U,0.U)
  configPEnext := Mux(configPEcnt <(cgrarows * cgracols -1).U,configPEcnt +1.U,0.U)
  config_finish := (configwaddr === peendaddr.U ) &&(configPEcnt === (cgrarows * cgracols -1).U)
  io.axistream_s.ready:= state("config") === ctrlregs(CGRAstateIndex) | state("loaddata") === ctrlregs(CGRAstateIndex)
  when(ctrlregs(CGRAstateIndex) === state("config") && io.axistream_s.valid && io.axistream_s.ready){
    configwdata := io.axistream_s.data
    configwen := true.B
    configwaddr := Mux(config_finish,0.U,configwaddrnext)
    when(configwaddr === peendaddr.U){
      configPEcnt := Mux(config_finish,0.U,configPEnext)
    }
  }

  //loaddata state
  //getresult state
  io.axistream_m.valid := (ctrlregs(CGRAstateIndex) === state("getresult")) && (ctrlregs(CGRAdatamemaddaddrIndex) < ctrlregs(CGRAdatamemreadlengthIndex)) &&(VecInit(Datamem.map(mem=>mem.io.memoptvalid)).apply(ctrlregs(CGRAdatamemIndex)))
  io.axistream_m.data := VecInit(Datamem.map(mem=>mem.io.rdata)).apply(ctrlregs(CGRAdatamemIndex))
  io.axistream_m.last := (ctrlregs(CGRAstateIndex) === state("getresult")) && (ctrlregs(CGRAdatamemaddaddrIndex) === ctrlregs(CGRAdatamemreadlengthIndex) - 1.U)
  //regs update
    ctrlregnextmap +=(CGRAfinishIndex -> cgrafinish)
    ctrlregwenmap +=(CGRAfinishIndex -> cgrafinish)

    ctrlregnextmap += (CGRAstateIndex -> statenext)
    ctrlregwenmap +=(CGRAstateIndex -> (config_finish | cgrafinish))
    
    ctrlregnextmap += (CGRAdatamemaddaddrIndex-> (ctrlregs(CGRAdatamemaddaddrIndex) + 1.U))
    ctrlregwenmap +=(CGRAdatamemaddaddrIndex->(((ctrlregs(CGRAstateIndex) === state("loaddata"))&&io.axistream_s.valid && io.axistream_s.ready)|((ctrlregs(CGRAstateIndex) === state("getresult")&&io.axistream_m.valid && io.axistream_m.ready)&&(ctrlregs(CGRAdatamemaddaddrIndex) < ctrlregs(CGRAdatamemreadlengthIndex)))))

  (0 until CGRActrlregsNum).foreach {i =>
    if(ctrlregnextmap.contains(i) && ctrlregwenmap.contains(i)){
      when(ctrlregs_axil_wen){
        ctrlregs(currentAddressw) := ctrlregs_axil_wdata
      }.elsewhen(ctrlregwenmap(i) === true.B){
        ctrlregs(i) := ctrlregnextmap(i)
      }
    }else{
      when(ctrlregs_axil_wen){
        ctrlregs(currentAddressw) := ctrlregs_axil_wdata
      }
    }
  }
}
