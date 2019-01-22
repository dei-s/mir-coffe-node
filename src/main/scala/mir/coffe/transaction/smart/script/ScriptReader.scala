package mir.coffe.transaction.smart.script

import mir.coffe.crypto
import mir.coffe.lang.ScriptVersion
import mir.coffe.lang.v1.Serde
import mir.coffe.transaction.ValidationError.ScriptParseError
import mir.coffe.transaction.smart.script.v1.ScriptV1

object ScriptReader {

  val checksumLength = 4

  def fromBytes(bytes: Array[Byte]): Either[ScriptParseError, Script] = {
    val checkSum         = bytes.takeRight(checksumLength)
    val computedCheckSum = crypto.secureHash(bytes.dropRight(checksumLength)).take(checksumLength)
    val version          = bytes.head
    val scriptBytes      = bytes.drop(1).dropRight(checksumLength)

    for {
      _ <- Either.cond(checkSum.sameElements(computedCheckSum), (), ScriptParseError("Invalid checksum"))
      sv <- ScriptVersion
        .fromInt(version)
        .fold[Either[ScriptParseError, ScriptVersion]](Left(ScriptParseError(s"Invalid version: $version")))(v => Right(v))
      script <- ScriptV1
        .validateBytes(scriptBytes)
        .flatMap { _ =>
          Serde.deserialize(scriptBytes).flatMap(ScriptV1(sv, _, checkSize = false))
        }
        .left
        .map(ScriptParseError)
    } yield script
  }

}
