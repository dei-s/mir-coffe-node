package mir.coffe.matcher

import java.io.File
import java.nio.file.Files.createTempDirectory

import akka.persistence.snapshot.SnapshotStoreSpec
import com.typesafe.config.ConfigFactory.parseString
import mir.coffe.TestHelpers.deleteRecursively
import mir.coffe.settings.loadConfig
import MatcherSnapshotStoreSpec.DirKey

class MatcherSnapshotStoreSpec extends SnapshotStoreSpec(loadConfig(parseString(s"""$DirKey = ${createTempDirectory("matcher").toAbsolutePath}
         |akka {
         |  actor.allow-java-serialization = on
         |  persistence.snapshot-store.plugin = coffe.matcher.snapshot-store
         |}""".stripMargin))) {
  protected override def afterAll(): Unit = {
    super.afterAll()
    deleteRecursively(new File(system.settings.config.getString(DirKey)).toPath)
  }
}

object MatcherSnapshotStoreSpec {
  val DirKey = "coffe.matcher.snapshot-store.dir"
}
