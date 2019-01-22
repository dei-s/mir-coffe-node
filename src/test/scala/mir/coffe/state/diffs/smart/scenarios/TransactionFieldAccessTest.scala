package mir.coffe.state.diffs.smart.scenarios

import mir.coffe.lang.v1.compiler.CompilerV1
import mir.coffe.lang.v1.parser.Parser
import mir.coffe.state.diffs.smart._
import mir.coffe.state._
import mir.coffe.state.diffs.{assertDiffAndState, assertDiffEi, produce}
import mir.coffe.utils.compilerContext
import mir.coffe.{NoShrink, TransactionGen}
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}
import mir.coffe.lagonaki.mocks.TestBlock
import mir.coffe.lang.ScriptVersion.Versions.V1
import mir.coffe.transaction.GenesisTransaction
import mir.coffe.transaction.lease.LeaseTransaction
import mir.coffe.transaction.smart.SetScriptTransaction
import mir.coffe.transaction.transfer._

class TransactionFieldAccessTest extends PropSpec with PropertyChecks with Matchers with TransactionGen with NoShrink {

  private def preconditionsTransferAndLease(
      code: String): Gen[(GenesisTransaction, SetScriptTransaction, LeaseTransaction, TransferTransactionV2)] = {
    val untyped = Parser(code).get.value
    val typed   = CompilerV1(compilerContext(V1, isAssetScript = false), untyped).explicitGet()._1
    preconditionsTransferAndLease(typed)
  }

  private val script =
    """
      |
      | match tx {
      | case ttx: TransferTransaction =>
      |       isDefined(ttx.assetId)==false
      |   case other =>
      |       false
      | }
      """.stripMargin

  property("accessing field of transaction without checking its type first results on exception") {
    forAll(preconditionsTransferAndLease(script)) {
      case ((genesis, script, lease, transfer)) =>
        assertDiffAndState(Seq(TestBlock.create(Seq(genesis, script))), TestBlock.create(Seq(transfer)), smartEnabledFS) { case _ => () }
        assertDiffEi(Seq(TestBlock.create(Seq(genesis, script))), TestBlock.create(Seq(lease)), smartEnabledFS)(totalDiffEi =>
          totalDiffEi should produce("TransactionNotAllowedByScript"))
    }
  }
}
