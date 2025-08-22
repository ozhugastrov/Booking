package inc.zhugastrov.booking

import cats.effect.*
import inc.zhugastrov.booking.db.BookingDAO
import inc.zhugastrov.booking.kafka.{KafkaConsumerService, KafkaProducerService}
import inc.zhugastrov.booking.routes.BookingRouts.bookingRoutes
import inc.zhugastrov.booking.server.Server
import inc.zhugastrov.booking.service.BookingService
import org.http4s.server.Server

object Main extends IOApp {

  private def program: IO[Resource[IO, Server]] = {
    for {
      consumer <- IO.apply(KafkaConsumerService())
      _ <- consumer.startConsuming()
      producer <- IO.apply(KafkaProducerService())
      db <- BookingDAO.create
      service <- IO.apply(BookingService(db, producer))
      server <- IO.apply(Server.createServer[IO](bookingRoutes(service)))
    } yield server
  }

  def run(args: List[String]): IO[ExitCode] = program.flatMap(_.use(_ => IO.never)).as(ExitCode.Success)
}