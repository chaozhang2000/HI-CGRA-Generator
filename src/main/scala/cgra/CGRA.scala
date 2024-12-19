package cgra
import utils._
import chisel3._
import chisel3.util._
import scala.collection.mutable.Map

class StreaminIO(size: Int) extends Bundle {
    val valid = Input(Bool())
    val data = Input(UInt(size.W))
}
class StreamoutIO(size: Int) extends Bundle {
    val valid = Output(Bool())
    val data = Output(UInt(size.W))
}

class CGRA extends Module with CGRAparams{
    val io = IO(new Bundle {
          val finish = Output(Bool())

          val axilite_s = new utils.AXI4LiteSlaveIO 
          val axistream_s = new utils.AXI4StreamSlaveIO(64)
          val streamin = Vec(loadFifoNum,new StreaminIO(dwidth))
          val streamout = Vec(storeFifoNum,new StreamoutIO(dwidth))
          val trigger = Output(Bool())
    })


    val PEs = Array.tabulate(cgrarows * cgracols)(i => Module(new PE(i)))
    val Links = Array.fill(2 *cgrarows*(cgracols-1) + 2*cgracols * (cgrarows -1))(Module(new Link))
    //val LoadMemfifo = Array.fill(loadFifoNum)(Module(new MemFifo(dataMemdWidth,loadFifoSize)))
    //val StoreMemfifo = Array.fill(storeFifoNum)(Module(new MemFifo(dataMemdWidth,storeFifoSize)))

    val ctrlregs = RegInit(VecInit(Seq.fill(CGRActrlregsNum)(0.U(CGRActrlregsdWidth.W))))
    var ctrlregnextmap = Map.empty[Int,UInt]
    var ctrlregwenmap = Map.empty[Int,Bool]

    val configwaddr = RegInit(0.U(dwidth.W))
    val configPEcnt = RegInit(0.U(dwidth.W))
    val configwdata = Wire(UInt(dwidth.W))
    val configwen = Wire(Bool())
  val configallpe = Wire(Bool())
  val configonepe = Wire(Bool())

  val configallpepipe = Wire(Bool())
  val configwdatapipe = Wire(UInt(dwidth.W))
  val configallpewaddrpipe = Wire(UInt(dwidth.W))

    configwdata := 0.U;configwen:=false.B

    io.trigger := ctrlregs(DatasrctriggerIndex) === 1.U
    val state = Map(
      "idle" -> 0.U,
      "config" -> 1.U,
      "loaddata" -> 2.U,
      "exe" -> 3.U,
      "getresult" -> 4.U
    )

    // init PEs io.inLinks
    val currentAddressw = RegInit(0.U(32.W))
    for( i<-0 until cgrarows * cgracols ) {
      (0 until pelinkNum ).foreach { linkindex => PEs(i).io.inLinks(linkindex):= 0.U}
          PEs(i).io.wen := Mux(configallpepipe,configwen,configwen &&(configPEcnt === i.U))
          PEs(i).io.waddr:=Mux(configallpepipe,configallpewaddrpipe+pectrlregsStartaddr.U,configwaddr)
          PEs(i).io.wdata:=configwdata
          PEs(i).io.run := ctrlregs(CGRAstateIndex) === state("exe")
    }

    //connect fifos
    //
    (0 until loadFifoNum).foreach{ i =>
      val dataindelay1 = RegInit(UInt(dwidth.W),0.U)
      val dataindelay2 = RegInit(UInt(dwidth.W),0.U)
      val dataindelay3 = RegInit(UInt(dwidth.W),0.U)
      val dataindelay4 = RegInit(UInt(dwidth.W),0.U)
      dataindelay1 := io.streamin(i).data
      dataindelay2 := dataindelay1 
      dataindelay3 := dataindelay2 
      dataindelay4 := dataindelay3 
      val validindelay1 = RegInit(Bool(),false.B)
      val validindelay2 = RegInit(Bool(),false.B)
      val validindelay3 = RegInit(Bool(),false.B)
      val validindelay4 = RegInit(Bool(),false.B)
      validindelay1 := io.streamin(i).valid
      validindelay2 := validindelay1 
      validindelay3 := validindelay2 
      validindelay4 := validindelay3 
      val peid2m = Wire(UInt(log2Ceil(cgrarows*cgracols).W))
      peid2m := PriorityMux(loadfifoaccess(i).map{peid => (PEs(peid).io.datamemio.ren)->PEs(peid).io.datamemio.peid2m})
      loadfifoaccess(i).foreach { peid =>
        PEs(peid).io.datamemio.rdata := dataindelay4
        PEs(peid).io.datamemio.peidfm := peid2m
        PEs(peid).io.datamemio.memoptvalid := validindelay4
      }
    }
    (0 until storeFifoNum).foreach{ i =>
      val peid2m1 = Wire(UInt(log2Ceil(cgrarows*cgracols).W))
      peid2m1 := PriorityMux(storefifoaccess(i).map{peid => (PEs(peid).io.datamemio.wen)->PEs(peid).io.datamemio.peid2m})
      storefifoaccess(i).foreach { peid =>
        PEs(peid).io.datamemio.rdata := 0.U
        PEs(peid).io.datamemio.peidfm :=peid2m1
        PEs(peid).io.datamemio.memoptvalid := true.B
      }
        
        io.streamout(i).data := PriorityMux(storefifoaccess(i).map{peid => (PEs(peid).io.datamemio.wen -> PEs(peid).io.datamemio.wdata)})
        io.streamout(i).valid := storefifoaccess(i).map{peid => PEs(peid).io.datamemio.wen}.reduce(_ | _)
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
  }.elsewhen(io.streamin.map(_.valid).reduce(_&&_)){
    statenext := 3.U
  }
  //config state
  val configPEnext = Wire(UInt(dwidth.W))
  val configwaddrnext = Wire(UInt(dwidth.W))
  configwaddrnext := Mux((configwaddr< peendaddr.U),configwaddr+ 1.U,0.U)
  configPEnext := Mux(configPEcnt <(cgrarows * cgracols -1).U,configPEcnt +1.U,0.U)
  config_finish := (configwaddr === peendaddr.U ) &&(configPEcnt === (cgrarows * cgracols -1).U)
  io.axistream_s.ready:= state("config") === ctrlregs(CGRAstateIndex) | state("loaddata") === ctrlregs(CGRAstateIndex)

  configonepe := ctrlregs(CGRAstateIndex) === state("config") && io.axistream_s.valid && io.axistream_s.ready;
  configallpe := ctrlregs(CGRAstateIndex) === state("config") && ctrlregs_axil_wen && (((currentAddressw >= ALL_I_initIndex.U) && (currentAddressw <=ALL_K_threadIndex.U))|((currentAddressw >= ALL_K_Index.U) && (currentAddressw <= ALL_I_Index.U)))
  //configallpe := 0.B
  PipelineConnect(configallpe,configallpepipe,ctrlregs(CGRAstateIndex)=/=state("config"))
  PipelineConnect(io.axilite_s.wdata.bits &mask,configwdatapipe,ctrlregs(CGRAstateIndex)=/=state("config"))
  PipelineConnect(currentAddressw,configallpewaddrpipe,ctrlregs(CGRAstateIndex)=/=state("config"))
  when(configonepe){
    configwdata := io.axistream_s.data(31,0)
    configwen := true.B
    configwaddr := Mux(config_finish,0.U,configwaddrnext)
    when(configwaddr === peendaddr.U){
      configPEcnt := Mux(config_finish,0.U,configPEnext)
    }
  }.elsewhen(configallpepipe){
    configwdata := configwdatapipe
    configwen := true.B
    configwaddr := configwaddr
    configPEcnt := configPEcnt
  }.otherwise{
    configwdata := 0.U
    configwen := false.B
    configwaddr := configwaddr
    configPEcnt := configPEcnt
  }

  //loaddata state
  //getresult state
  //regs update
    ctrlregnextmap +=(CGRAfinishIndex -> cgrafinish)
    ctrlregwenmap +=(CGRAfinishIndex -> cgrafinish)

    ctrlregnextmap += (CGRAstateIndex -> statenext)
    ctrlregwenmap +=(CGRAstateIndex -> (config_finish | cgrafinish |io.streamin.map(_.valid).reduce(_&&_)))
    

  (0 until CGRActrlregsNum).foreach {i =>
    if(ctrlregnextmap.contains(i) && ctrlregwenmap.contains(i)){
      when(ctrlregs_axil_wen && (~configallpe)){
        ctrlregs(currentAddressw) := ctrlregs_axil_wdata
      }.elsewhen(ctrlregwenmap(i) === true.B){
        ctrlregs(i) := ctrlregnextmap(i)
      }
    }else{
      when(ctrlregs_axil_wen && (~configallpe)){
        ctrlregs(currentAddressw) := ctrlregs_axil_wdata
      }
    }
  }

}
