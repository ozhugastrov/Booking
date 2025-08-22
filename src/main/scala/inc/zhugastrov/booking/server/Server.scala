package inc.zhugastrov.booking.server

import cats.effect.std.Console
import cats.effect.{Async, Resource}
import cats.syntax.all.*
import com.comcast.ip4s.*
import fs2.text
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.dsl.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.middleware.Logger
import org.typelevel.ci.CIString


object Server {

  def createServer[F[_] : Async : Console](
                                           service: HttpRoutes[F]
                                         ): Resource[F, Server] = {
    val loggerService = Logger.httpRoutesLogBodyText[F](
      logHeaders = true,
      logBody = r => Some(r.through(text.utf8.decode).compile.string),
      redactHeadersWhen = _ => false,
      logAction = Some((msg: String) => Console[F].println(msg))
    )(service).orNotFound

    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(loggerService)
      .build
  }
}




