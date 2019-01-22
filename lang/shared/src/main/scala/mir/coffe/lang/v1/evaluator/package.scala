package mir.coffe.lang.v1

import cats.data.EitherT
import mir.coffe.lang.v1.evaluator.ctx.LoggedEvaluationContext
import mir.coffe.lang.v1.task.TaskM
import mir.coffe.lang.{ExecutionError, TrampolinedExecResult}
import monix.eval.Coeval

package object evaluator {
  type EvalM[A] = TaskM[LoggedEvaluationContext, ExecutionError, A]

  implicit class EvalMOps[A](ev: EvalM[A]) {
    def ter(ctx: LoggedEvaluationContext): TrampolinedExecResult[A] = {
      EitherT(ev.run(ctx).map(_._2))
    }
  }

  def liftTER[A](ter: Coeval[Either[ExecutionError, A]]): EvalM[A] = {
    TaskM(_ => ter)
  }
}
