package mir.coffe.lang

trait Versioned {
  type Ver <: ScriptVersion
  val version: Ver
}
