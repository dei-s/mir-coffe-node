package mir.coffe.state.diffs.smart.scenarios

import java.nio.charset.StandardCharsets

import mir.coffe.account.AddressScheme
import mir.coffe.lang.{Testing, Global}
import mir.coffe.lang.ScriptVersion.Versions.V1
import mir.coffe.lang.v1.compiler.CompilerV1
import mir.coffe.lang.v1.compiler.Terms.EVALUATED
import mir.coffe.lang.v1.evaluator.EvaluatorV1
import mir.coffe.lang.v1.parser.Parser
import mir.coffe.state._
import mir.coffe.state.diffs._
import mir.coffe.state.diffs.smart._
import mir.coffe.transaction.assets.IssueTransactionV2
import mir.coffe.transaction.smart.script.v1.ScriptV1
import mir.coffe.transaction.transfer._
import mir.coffe.transaction.{DataTransaction, GenesisTransaction}
import mir.coffe.utils._
import mir.coffe.{NoShrink, TransactionGen}
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}

class NotaryControlledTransferScenarioTest extends PropSpec with PropertyChecks with Matchers with TransactionGen with NoShrink {
  val preconditions: Gen[
    (Seq[GenesisTransaction], IssueTransactionV2, DataTransaction, TransferTransactionV1, DataTransaction, DataTransaction, TransferTransactionV1)] =
    for {
      company  <- accountGen
      king     <- accountGen
      notary   <- accountGen
      accountA <- accountGen
      accountB <- accountGen
      ts       <- timestampGen
      genesis1 = GenesisTransaction.create(company, ENOUGH_AMT, ts).explicitGet()
      genesis2 = GenesisTransaction.create(king, ENOUGH_AMT, ts).explicitGet()
      genesis3 = GenesisTransaction.create(notary, ENOUGH_AMT, ts).explicitGet()
      genesis4 = GenesisTransaction.create(accountA, ENOUGH_AMT, ts).explicitGet()
      genesis5 = GenesisTransaction.create(accountB, ENOUGH_AMT, ts).explicitGet()

      assetScript = s"""
                    |
                    | match tx {
                    |   case ttx: TransferTransaction =>
                    |      let king = Address(base58'${king.address}')
                    |      let company = Address(base58'${company.address}')
                    |      let notary1 = addressFromPublicKey(extract(getBinary(king, "notary1PK")))
                    |      let txIdBase58String = toBase58String(ttx.id)
                    |      let isNotary1Agreed = match getBoolean(notary1,txIdBase58String) {
                    |        case b : Boolean => b
                    |        case _ : Unit => false
                    |      }
                    |      let recipientAddress = addressFromRecipient(ttx.recipient)
                    |      let recipientAgreement = getBoolean(recipientAddress,txIdBase58String)
                    |      let isRecipientAgreed = if(isDefined(recipientAgreement)) then extract(recipientAgreement) else false
                    |      let senderAddress = addressFromPublicKey(ttx.senderPublicKey)
                    |      senderAddress.bytes == company.bytes || (isNotary1Agreed && isRecipientAgreed)
                    |   case other => throw()
                    | }
        """.stripMargin

      untypedScript = Parser(assetScript).get.value

      typedScript = ScriptV1(CompilerV1(compilerContext(V1, isAssetScript = false), untypedScript).explicitGet()._1).explicitGet()

      issueTransaction = IssueTransactionV2
        .selfSigned(
          2,
          AddressScheme.current.chainId,
          company,
          "name".getBytes(StandardCharsets.UTF_8),
          "description".getBytes(StandardCharsets.UTF_8),
          100,
          0,
          false,
          Some(typedScript),
          1000000,
          ts
        )
        .explicitGet()

      assetId = issueTransaction.id()

      kingDataTransaction = DataTransaction
        .selfSigned(1, king, List(BinaryDataEntry("notary1PK", ByteStr(notary.publicKey))), 1000, ts + 1)
        .explicitGet()

      transferFromCompanyToA = TransferTransactionV1
        .selfSigned(Some(assetId), company, accountA, 1, ts + 20, None, 1000, Array.empty)
        .explicitGet()

      transferFromAToB = TransferTransactionV1
        .selfSigned(Some(assetId), accountA, accountB, 1, ts + 30, None, 1000, Array.empty)
        .explicitGet()

      notaryDataTransaction = DataTransaction
        .selfSigned(1, notary, List(BooleanDataEntry(transferFromAToB.id().base58, true)), 1000, ts + 4)
        .explicitGet()

      accountBDataTransaction = DataTransaction
        .selfSigned(1, accountB, List(BooleanDataEntry(transferFromAToB.id().base58, true)), 1000, ts + 5)
        .explicitGet()
    } yield
      (Seq(genesis1, genesis2, genesis3, genesis4, genesis5),
       issueTransaction,
       kingDataTransaction,
       transferFromCompanyToA,
       notaryDataTransaction,
       accountBDataTransaction,
       transferFromAToB)

  private def eval(code: String) = {
    val untyped = Parser(code).get.value
    val typed   = CompilerV1(compilerContext(V1, isAssetScript = false), untyped).map(_._1)
    typed.flatMap(EvaluatorV1[EVALUATED](dummyEvalContext(V1), _))
  }

  property("Script toBase58String") {
    val s = "AXiXp5CmwVaq4Tp6h6"
    eval(s"""toBase58String(base58'$s') == \"$s\"""") shouldBe Testing.evaluated(true)
  }

  property("Script toBase64String") {
    val s = "Kl0pIkOM3tRikA=="
    eval(s"""toBase64String(base64'$s') == \"$s\"""") shouldBe Testing.evaluated(true)
  }

  property("addressFromString() returns None when address is too long") {
    val longAddress = "A" * (Global.MaxBase58String + 1)
    eval(s"""addressFromString("$longAddress")""") shouldBe Left("base58Decode input exceeds 100")
  }

  property("Scenario") {
    forAll(preconditions) {
      case (genesis, issue, kingDataTransaction, transferFromCompanyToA, notaryDataTransaction, accountBDataTransaction, transferFromAToB) =>
        assertDiffAndState(smartEnabledFS) { append =>
          append(genesis).explicitGet()
          append(Seq(issue, kingDataTransaction, transferFromCompanyToA)).explicitGet()
          append(Seq(transferFromAToB)) should produce("NotAllowedByScript")
          append(Seq(notaryDataTransaction)).explicitGet()
          append(Seq(transferFromAToB)) should produce("NotAllowedByScript") //recipient should accept tx
          append(Seq(accountBDataTransaction)).explicitGet()
          append(Seq(transferFromAToB)).explicitGet()
        }
    }
  }
}
