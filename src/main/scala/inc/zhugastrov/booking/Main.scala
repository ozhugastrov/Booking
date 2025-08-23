package inc.zhugastrov.booking

import cats.effect.*
import inc.zhugastrov.booking.db.{BookingDAO, Dependencies}
import inc.zhugastrov.booking.kafka.{KafkaConsumerService, KafkaProducerService}
import inc.zhugastrov.booking.routes.BookingRoute.bookingRoutes
import inc.zhugastrov.booking.server.Server
import inc.zhugastrov.booking.service.BookingService
import org.http4s.server.Server

object Main extends IOApp {

  private def program: IO[Resource[IO, Server]] = {
    for {
      db <- BookingDAO.create(Dependencies.xa)
      consumer <- IO.apply(KafkaConsumerService(db))
      consumer <- consumer.startConsuming()
      producer <- IO.apply(KafkaProducerService("kafka:9092"))
      service <- IO.apply(BookingService(db, producer))
      server <- IO.apply(Server.createServer[IO](bookingRoutes(service)))
    } yield server
  }

  def run(args: List[String]): IO[ExitCode] = program.flatMap(_.use(_ => IO.never)).as(ExitCode.Success)
}