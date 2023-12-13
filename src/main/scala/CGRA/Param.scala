package top

trait Crossbarparams {
  val crossbarInputNum = 5
  val crossbarOutputNum = 6
  val crossbarDataWidth = 32
}

trait TileParams extends Crossbarparams {
  val tileInputNum = 4
  val tileOutputNum = 4
}
