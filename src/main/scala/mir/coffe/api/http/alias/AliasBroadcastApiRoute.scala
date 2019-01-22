package mir.coffe.api.http.alias

import akka.http.scaladsl.server.Route
import mir.coffe.api.http._
import mir.coffe.http.BroadcastRoute
import mir.coffe.settings.RestAPISettings
import mir.coffe.utx.UtxPool
import io.netty.channel.group.ChannelGroup

case class AliasBroadcastApiRoute(settings: RestAPISettings, utx: UtxPool, allChannels: ChannelGroup) extends ApiRoute with BroadcastRoute {
  override val route = pathPrefix("alias" / "broadcast") {
    signedCreate
  }

  def signedCreate: Route = (path("create") & post) {
    json[SignedCreateAliasV1Request] { aliasReq =>
      doBroadcast(aliasReq.toTx)
    }
  }
}
