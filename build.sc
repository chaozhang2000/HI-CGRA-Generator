import mill._, scalalib._

/**
 * Scala 2.12 module that is source-compatible with 2.11.
 * This is due to Chisel's use of structural types. See
 * https://github.com/freechipsproject/chisel3/issues/606
 */
trait HasXsource211 extends ScalaModule {
  override def scalacOptions = T {
    super.scalacOptions() ++ Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-language:reflectiveCalls",
      "-Xsource:2.11"
    )
  }
}

trait HasChisel3 extends ScalaModule {
  override def ivyDeps = Agg(
    ivy"edu.berkeley.cs::chisel3:3.5.3",ivy"edu.berkeley.cs::chiseltest:0.5.3"
 )
}

trait HasChiselTests extends CrossSbtModule  {
  object test extends Tests {
    override def ivyDeps = Agg(ivy"org.scalatest::scalatest:3.2.2", ivy"edu.berkeley.cs::chiseltest:0.5.3")
    def testFramework = "org.scalatest.tools.Framework"
  }
}

trait HasMacroParadise extends ScalaModule {
  // Enable macro paradise for @chiselName et al
  val macroPlugins = Agg(ivy"edu.berkeley.cs:::chisel3-plugin:3.5.3")
  def scalacPluginIvyDeps = macroPlugins
  def compileIvyDeps = macroPlugins
}

object chiselModule extends CrossSbtModule with HasChisel3 with HasChiselTests with HasXsource211 with HasMacroParadise {
  def crossScalaVersion = "2.12.13"
}

