package inc.zhugastrov.booking

import cats.effect.*
import inc.zhugastrov.booking.config.appConfig
import inc.zhugastrov.booking.db.Dependencies
import inc.zhugastrov.booking.db.impl.BookingDAOImpl
import inc.zhugastrov.booking.kafka.{KafkaConsumerService, KafkaProducerService}
import inc.zhugastrov.booking.routes.BookingRoute.bookingRoutes
import inc.zhugastrov.booking.server.Server
import inc.zhugastrov.booking.service.api.BookingService
import inc.zhugastrov.booking.service.impl.BookingServiceImpl
import org.http4s.server.Server

object Main extends IOApp {

  private def program: IO[Resource[IO, Server]] = {
    for {
      config <- appConfig.load[IO]
      db <- BookingDAOImpl.create(Dependencies.xa(config))
      consumer <- IO.apply(KafkaConsumerService(db, config))
      consumer <- consumer.startConsuming()
      producer <- IO.apply(KafkaProducerService(config.kafkaUrl))
      service <- BookingServiceImpl.create(db, producer)
      server <- IO.apply(Server.createServer[IO](bookingRoutes(service)))
    } yield server
  }

  def run(args: List[String]): IO[ExitCode] = program.flatMap(_.use(_ => IO.never)).as(ExitCode.Success)
}