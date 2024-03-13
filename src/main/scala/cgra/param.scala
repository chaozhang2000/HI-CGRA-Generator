package cgra

trait Commonparams{
  val dwidth = 32
  val awidth = 32
  val pelinkNum = 4
  val opts = List("nul","add","mul")
  val loopNum = 3
  val instmemNum = pelinkNum + 1 + 1
  val instmemSize = 8
  val instmemStartaddr = 0
  val constmemNum = 2
  val constmemSize = 8
  val constmemStartaddr = instmemStartaddr + instmemNum * instmemSize
  val shiftconstmemNum = 2
  val shiftconstmemSize = 8
  val shiftconstmemStartaddr = constmemStartaddr + constmemNum * constmemSize
  val datamemSize = 512
  val pectrlregsNum = 27
  val pectrlregsStartaddr = shiftconstmemStartaddr +shiftconstmemNum * shiftconstmemSize 
}

trait Crossbarparams extends Commonparams{
  val crossbarInputNum = pelinkNum + 1 + 1 + 1//fureg,furesult,zero
  val crossbarOutputNum = pelinkNum
  val crossbarDataWidth = dwidth
}

trait Aluparams extends Commonparams{
  val aluoptlist = opts
  val aluwidth = dwidth 
  val aluoptnum = aluoptlist.size
}

trait Srcmuxparams extends Commonparams{
  val srcmuxWidth = dwidth
  val srcmuxInputNum = pelinkNum + loopNum + 1 + 1;//1 fureg,1 constMem
}
trait InstMemparams extends Commonparams{
  val instMemdWidth = dwidth
  val instMemaWidth = awidth
  val instMemSize = instmemSize
  val instMemStartaddr = instmemStartaddr 
}
trait ConstMemparams extends Commonparams{
  val constMemdWidth = dwidth
  val constMemaWidth = awidth
  val constMemSize = constmemSize
  val constMemStartaddr = constmemStartaddr
}
trait ShiftconstMemparams extends Commonparams{
  val shiftconstMemdWidth = dwidth
  val shiftconstMemaWidth = awidth
  val shiftconstMemSize = shiftconstmemSize
  val shiftconstMemStartaddr = shiftconstmemStartaddr
}
trait DataMemparams extends Commonparams{
  val dataMemWidth = dwidth
  val dataMemSize = datamemSize
}

trait Instparams{
  val Shiftconst1startbit = 0
  val Shiftconst1bits = 1
  val Shiftconst2startbit = 1
  val Shiftconst2bits = 1
  val Fukeystartbit = 2
  val Fukeybits = 8
  val Src1keystartbit = 10
  val Src1keybits = 5
  val Src2keystartbit = 15
  val Src2keybits = 5
  val Linkkeystartbit = 20
  val Linkkeybits = 3
  
  val Srcconstcode = 2
}

trait PEctrlregsparams extends Commonparams{
  val PEctrlregsNum = pectrlregsNum 
  val PEctrlregsaWidth = awidth
  val PEctrlregsdWidth = dwidth
  val PEctrlregsStartaddr = pectrlregsStartaddr 
	val InstnumIndex = 0;
	val IInumIndex= 1;
	val FinishInstcntIndex= 2;
	val FinishIIcntIndex=3 ;
	val Constnum1Index= 4;
	val Constnum2Index= 5;
	val Shiftconstnum1Index=6 ;
	val Shiftconstnum2Index=7 ;
	val I_initIndex= 8;
	val J_initIndex= 9;
	val K_initIndex= 10;
	val I_incIndex= 11;
	val J_incIndex= 12;
	val K_incIndex= 13;
	val I_threadIndex=14 ;
	val J_threadIndex= 15;
	val K_threadIndex= 16;
  
	val InstcntIndex= 17;
	val IIcntIndex= 18;
	val Constcnt1Index= 19;
	val Constcnt2Index= 20;
	val Shiftconstcnt1Index= 21;
	val Shiftconstcnt2Index= 22;
	val KIndex= 23;
	val JIndex= 24;
	val IIndex= 25;
	val FinishIndex= 26;
}

trait CGRAparams extends Commonparams with Crossbarparams with Aluparams with Srcmuxparams with InstMemparams with ConstMemparams with ShiftconstMemparams with DataMemparams with Instparams with PEctrlregsparams{

}
