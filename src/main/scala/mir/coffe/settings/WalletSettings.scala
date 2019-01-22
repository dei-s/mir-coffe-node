package mir.coffe.settings

import java.io.File

import mir.coffe.state.ByteStr

case class WalletSettings(file: Option[File], password: Option[String], seed: Option[ByteStr])
