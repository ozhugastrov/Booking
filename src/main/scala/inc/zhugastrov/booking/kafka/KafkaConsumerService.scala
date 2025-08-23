package inc.zhugastrov.booking.kafka

import cats.effect.*
import io.circe.generic.auto.*
import io.circe.parser.decode
import fs2.*
import fs2.kafka.*
import inc.zhugastrov.booking.db.BookingDAO
import inc.zhugastrov.booking.domain.DoubleBookingResponse

class KafkaConsumerService(db: BookingDAO) {

  private def processRecord(record: ConsumerRecord[Long, String]) = {
    decode[DoubleBookingResponse](record.value).fold(
      err =>  IO.println("Unknown event received " + record.value),
      event => db.storeBookingConflict(event) *> IO.println("Stored to db"))
  }

  private def consumerSettings: ConsumerSettings[IO, Long, String] =
    ConsumerSettings[IO, Long, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("kafka:9092")
      .withGroupId("bookings")

  def consumer: Stream[IO, Unit] = {
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo("booking")
      .records
      .mapAsync(5) { committable =>
        processRecord(committable.record) *> committable.offset.commit
      }
  }

  def startConsuming(): IO[FiberIO[Unit]] =
    consumer
      .handleErrorWith { error =>
        Stream.eval(IO.println(s"Consumer error: ${error.getMessage}")) >>
          Stream.raiseError[IO](error)
      }
      .compile
      .drain
      .start
}
