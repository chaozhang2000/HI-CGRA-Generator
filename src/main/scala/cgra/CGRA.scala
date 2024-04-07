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
    })

    val PEs = Array.fill(cgrarows * cgracols)(Module(new PE))
    val Links = Array.fill(2 *cgrarows*(cgracols-1) + 2*cgracols * (cgrarows -1))(Module(new Link))
    val Datamem= Array.fill(cgrarows*cgracols)(Module(new Datamem))

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
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(0);
          PEs((r+1)*cgracols+c).io.inLinks(1) := Links(linkid).io.out;
          linkid += 1
        }
        if(r > 0){//to S
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(1);
          PEs((r-1)*cgracols+c).io.inLinks(0) := Links(linkid).io.out;
          linkid += 1
        }
        if(c < cgracols -1){//to E
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(3);
          PEs(r*cgracols+c+1).io.inLinks(2) := Links(linkid).io.out;
          linkid += 1
        }
        if(c > 0){//to W
          Links(linkid).io.in := PEs(r*cgracols + c).io.outLinks(2);
          PEs(r*cgracols+c-1).io.inLinks(3) := Links(linkid).io.out;
          linkid += 1
        }
      }
    }

    io.finish := PEs.map(_.io.finish).reduce(_ && _)
}
