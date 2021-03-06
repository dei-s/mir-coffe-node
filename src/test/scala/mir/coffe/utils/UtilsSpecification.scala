package mir.coffe.utils

import mir.coffe.lang.v1.compiler.Terms.{FUNCTION_CALL, TRUE}
import mir.coffe.lang.v1.compiler.Types.BOOLEAN
import mir.coffe.lang.v1.evaluator.ctx.{EvaluationContext, UserFunction}
import org.scalatest.{FreeSpec, Matchers}

class UtilsSpecification extends FreeSpec with Matchers {

  "estimate()" - {
    "handles functions that depend on each other" in {
      val callee = UserFunction("callee", BOOLEAN, "test users true")(TRUE)
      val caller = UserFunction("caller", BOOLEAN, "test call")(FUNCTION_CALL(callee.header, List.empty))
      val ctx = EvaluationContext(
        typeDefs = Map.empty,
        letDefs = Map.empty,
        functions = Seq(caller, callee).map(f => f.header -> f)(collection.breakOut)
      )
      estimate(ctx).size shouldBe 2
    }
  }
}
