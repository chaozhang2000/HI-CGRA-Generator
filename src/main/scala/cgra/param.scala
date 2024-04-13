package cgra

trait Commonparams{
  val cgrarows = 3
  val cgracols = 3
  val dwidth = 32
  val awidth = 32
  val cgractrlregsnum = 6
  val cgrastartaddr = 0

  //PE
  val pelinkNum = 4
  val opts = List("nul","add","mul","getelementptr","shl","load","store")
  val srcnum = 2
  val loopNum = 3
  val instmemNum = pelinkNum + 1 + 1
  val instmemSize = 8
  val instmemStartaddr = 0
  val instmemindex = 0
  val fudelayindex = 1
  val linkdelayindex = 2
  val constmemNum = 2
  val constmemSize = 8
  val constmemStartaddr = instmemStartaddr + instmemNum * instmemSize
  val shiftconstmemNum = 2
  val shiftconstmemSize = 8
  val shiftconstmemStartaddr = constmemStartaddr + constmemNum * constmemSize
  val datamemSize = 512
  val pectrlregsNum = 29
  val pectrlregsStartaddr = shiftconstmemStartaddr +shiftconstmemNum * shiftconstmemSize 
  val peendaddr = pectrlregsStartaddr + pectrlregsNum -1
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
  val dataMemdWidth = dwidth
  val dataMemaWidth = awidth
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
  
  val Linkemptycode = 0
  val Srcconstcode = 1
}

trait PEctrlregsparams extends Commonparams{
  val PEctrlregsNum = pectrlregsNum 
  val PEctrlregsaWidth = awidth
  val PEctrlregsdWidth = dwidth
  val PEctrlregsStartaddr = pectrlregsStartaddr 
	val InstnumIndex = 0
	val IInumIndex= 1
  val StartcyclenumIndex= 2
	val FinishInstcntIndex= 3
	val FinishIIcntIndex=4 
	val Constnum1Index= 5
	val Constnum2Index= 6
	val Shiftconstnum1Index=7
	val Shiftconstnum2Index=8
	val I_initIndex= 9
	val J_initIndex= 10
	val K_initIndex= 11
	val I_incIndex= 12
	val J_incIndex= 13
	val K_incIndex= 14
	val I_threadIndex=15
	val J_threadIndex= 16
	val K_threadIndex= 17
  
  val StartcyclecntIndex=18
	val InstcntIndex= 19
	val IIcntIndex= 20
	val Constcnt1Index= 21
	val Constcnt2Index= 22
	val Shiftconstcnt1Index= 23
	val Shiftconstcnt2Index= 24
	val KIndex= 25
	val JIndex= 26
	val IIndex= 27
	val FinishIndex= 28
}

trait CGRActrlregsparams extends Commonparams{
  val CGRActrlregsNum = cgractrlregsnum 
  val CGRActrlregsaWidth = awidth
  val CGRActrlregsdWidth = dwidth

  val CGRAstateIndex = 0
  val CGRAfinishIndex = 1
  val CGRAdatamemIndex = 2
  val CGRAdatamemstartaddrIndex = 3
  val CGRAdatamemaddaddrIndex = 4
  val CGRAdatamemreadlengthIndex = 5
}

trait CGRAparams extends Commonparams with Crossbarparams with Aluparams with Srcmuxparams with InstMemparams with ConstMemparams with ShiftconstMemparams with DataMemparams with Instparams with PEctrlregsparams with CGRActrlregsparams {

}
