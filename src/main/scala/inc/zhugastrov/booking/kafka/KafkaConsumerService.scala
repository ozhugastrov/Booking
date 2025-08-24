package inc.zhugastrov.booking.kafka

import cats.effect.*
import io.circe.generic.auto.*
import io.circe.parser.decode
import fs2.*
import fs2.kafka.*
import inc.zhugastrov.booking.config.AppConfig
import inc.zhugastrov.booking.db.api.BookingDAO
import inc.zhugastrov.booking.domain.DoubleBookingResponse

class KafkaConsumerService(db: BookingDAO, config: AppConfig) {

  private def processRecord(record: ConsumerRecord[Long, String]) = {
    decode[DoubleBookingResponse](record.value).fold(
      err => IO.println("Unknown event received " + record.value),
      event => db.storeBookingConflict(event) *> IO.println("Stored to db"))
  }

  private def consumerSettings: ConsumerSettings[IO, Long, String] =
    ConsumerSettings[IO, Long, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(config.kafkaUrl)
      .withGroupId("bookings")

  def consume: IO[Unit] = {
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo("booking")
      .records
      .mapAsync(5) { committable =>
        processRecord(committable.record) *> committable.offset.commit
      }.compile.drain
  }

}
