package mir.coffe.transaction

trait VersionedTransaction {
  def version: Byte
}
