package mir.coffe.lang

import mir.coffe.lang.directives.Directive

trait ExprCompiler extends Versioned {
  def compile(input: String, directives: List[Directive]): Either[String, version.ExprT]
}
