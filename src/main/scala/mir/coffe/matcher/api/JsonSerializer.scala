package mir.coffe.matcher.api
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationContext, JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import mir.coffe.state.ByteStr
import mir.coffe.transaction.assets.exchange.AssetPair

object JsonSerializer {

  private val coreTypeSerializers = new SimpleModule()
  coreTypeSerializers.addDeserializer(classOf[AssetPair], new AssetPairDeserializer)

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(coreTypeSerializers)

  def serialize(value: Any): String                             = mapper.writeValueAsString(value)
  def deserialize[T](value: String)(implicit m: Manifest[T]): T = mapper.readValue(value)

  private class AssetPairDeserializer extends StdDeserializer[AssetPair](classOf[AssetPair]) {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): AssetPair = {
      val node = p.getCodec.readTree[JsonNode](p)
      def readAssetId(fieldName: String) = {
        val x = node.get(fieldName).asText(AssetPair.CoffeName)
        if (x == AssetPair.CoffeName) None else Some(ByteStr.decodeBase58(x).get)
      }

      AssetPair(readAssetId("amountAsset"), readAssetId("priceAsset"))
    }
  }

}
