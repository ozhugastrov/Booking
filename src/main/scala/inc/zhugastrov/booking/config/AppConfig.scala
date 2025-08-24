package inc.zhugastrov.booking.config

import cats.syntax.all.*
import ciris.circe.*
import ciris.*
import io.circe.Decoder

import java.nio.file.Paths

final case class AppConfig(kafkaUrl: String, postgresUrl: String, postgresName: String, postgresPass: Secret[String])

object AppConfig {
  given apiDecoder: Decoder[AppConfig] = Decoder.instance { h =>
    for {
      kafkaUrl <- h.get[String]("kafkaUrl")
      postgresUrl <- h.get[String]("postgresUrl")
      postgresName <- h.get[String]("postgresName")
      postgresPass <- h.get[String]("postgresPass")
    } yield AppConfig(kafkaUrl, postgresUrl, postgresName, Secret(postgresPass))
  }

  given apiConfigDecoder: ConfigDecoder[String, AppConfig] =
    circeConfigDecoder("AppConfig")
}

val appConfig = {
  lazy val confFile = file(Paths.get("src/main/resources/application.json")).as[AppConfig]
  (
    env("KAFKA_URL").as[String] or confFile.map(_.kafkaUrl),
    env("POSTGRES_URL").as[String] or confFile.map(_.postgresUrl),
    env("POSTGRES_NAME").as[String] or confFile.map(_.postgresName),
    env("POSTGRES_PASSWORD").as[Secret[String]] or confFile.map(_.postgresPass)
  ).parMapN(AppConfig.apply)
}