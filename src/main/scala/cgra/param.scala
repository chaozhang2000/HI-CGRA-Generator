package cgra

trait Commonparams{
  val dwidth = 32
  val awidth = 32
  val pelinkNum = 4
  val opts = List("add","mul")
  val loopNum = 3
  val instmemSize = 8
  val instmemStartaddr = 0
  val constmemSize = 8
  val shiftconstmemSize = 8
  val datamemSize = 512
}

trait Crossbarparams extends Commonparams{
  val crossbarInputNum = pelinkNum + 1//fureg
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
  val constMemWidth = dwidth
  val constMemSize = constmemSize
}
trait ShiftconstMemparams extends Commonparams{
  val shiftconstMemWidth = dwidth
  val shiftconstMemSize = shiftconstmemSize
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
}

trait CGRAparams extends Commonparams with Crossbarparams with Aluparams with Srcmuxparams with InstMemparams with ConstMemparams with ShiftconstMemparams with DataMemparams with Instparams{

}
