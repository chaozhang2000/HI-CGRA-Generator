package utils 

trait CrossbarTestParam {
  val inputNum = 5
  val outputNum = 6
  val dataWidth = 8
}

trait AluTestParam{
  val dataWidth = 32
  val opneed = List(
      "nul",
      "add",
      "sub",
      "mul",
      "div"
    )
  val functionNum = opneed.length
}
