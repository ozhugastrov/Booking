package inc.zhugastrov.booking

import cats.effect.*
import inc.zhugastrov.booking.config.appConfig
import inc.zhugastrov.booking.db.Dependencies
import inc.zhugastrov.booking.db.impl.BookingDAOImpl
import inc.zhugastrov.booking.kafka.{KafkaConsumerService, KafkaProducerService}
import inc.zhugastrov.booking.routes.BookingRoute.bookingRoutes
import inc.zhugastrov.booking.server.Server
import inc.zhugastrov.booking.service.impl.BookingServiceImpl
import org.http4s.server.{Router, Server}

object Main extends IOApp {

  private def program: Resource[IO, Server] = {
    for {
      config <- appConfig.load[IO].toResource
      db <- BookingDAOImpl.create(Dependencies.xa(config))
      producer <- Resource.pure(KafkaProducerService(config.kafkaUrl))
      consumer <- KafkaConsumerService(db, config).consume.background
      service <- BookingServiceImpl.create(db, producer)
      server <- Server.createServer[IO](Router("/api/v1" -> bookingRoutes(service)))
    } yield server
  }

  def run(args: List[String]): IO[ExitCode] = program.use(_ => IO.never).as(ExitCode.Success)
}