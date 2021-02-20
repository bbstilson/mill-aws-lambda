import com.amazonaws.services.lambda.runtime.events.S3Event

import java.net.URLDecoder
import scala.jdk.CollectionConverters._

class Main {

  def decodeS3Key(key: String): String =
    URLDecoder.decode(key.replace("+", " "), "utf-8")

  def getSourceBuckets(event: S3Event): java.util.List[String] = {
    val result = event.getRecords.asScala
      .map(record => decodeS3Key(record.getS3.getObject.getKey))
      .asJava

    println(result)

    result
  }
}
