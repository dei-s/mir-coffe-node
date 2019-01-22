package mir.coffe.transaction

import mir.coffe.account.PublicKeyAccount

trait Authorized {
  val sender: PublicKeyAccount
}
