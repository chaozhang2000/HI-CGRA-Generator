import mill._, scalalib._

/**
 * Scala 2.12 module that is source-compatible with 2.11.
 * This is due to Chisel's use of structural types. See
 * https://github.com/freechipsproject/chisel3/issues/606
 */
object chiselModule extends CrossSbtModule{
  override def ivyDeps = Agg(
    ivy"edu.berkeley.cs::chisel3:3.5.0",
    ivy"edu.berkeley.cs:chiseltest_2.12:0.3.3"
 )
  override def scalacOptions = Seq(
    "-Xsource:2.11"
    )
  def crossScalaVersion = "2.12.13"
  val macroPlugins = Agg(ivy"edu.berkeley.cs:::chisel3-plugin:3.5.0",ivy"org.scalatest::scalatest:3.2.2")
  def scalacPluginIvyDeps = macroPlugins
  def compileIvyDeps = macroPlugins
}

