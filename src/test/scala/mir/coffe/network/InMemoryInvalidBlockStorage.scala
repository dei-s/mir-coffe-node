package mir.coffe.network

import mir.coffe.state.ByteStr
import mir.coffe.transaction.ValidationError
import mir.coffe.transaction.ValidationError.GenericError

class InMemoryInvalidBlockStorage extends InvalidBlockStorage {

  var s: Set[ByteStr] = Set.empty[ByteStr]

  override def add(blockId: ByteStr, validationError: ValidationError): Unit = s += blockId

  override def find(blockId: ByteStr): Option[ValidationError] = {
    if (s.contains(blockId)) Some(GenericError("Unknown")) else None
  }

}
