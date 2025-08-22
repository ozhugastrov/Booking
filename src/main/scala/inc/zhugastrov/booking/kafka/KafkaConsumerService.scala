package inc.zhugastrov.booking.kafka

import cats.effect.*
import fs2.*
import fs2.kafka.*

class KafkaConsumerService {

  private def processRecord(record: ConsumerRecord[Long, String]) = IO.println("Consumed " + record.key)

  private def consumerSettings: ConsumerSettings[IO, Long, String] =
    ConsumerSettings[IO, Long, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("booking")

  def consumer: Stream[IO, Unit] = {
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo("booking")
      .records
      .mapAsync(5) { committable =>
        processRecord(committable.record) *> committable.offset.commit
      }
  }

  def startConsuming(): IO[Unit] =
    consumer
      .handleErrorWith { error =>
        Stream.eval(IO.println(s"Consumer error: ${error.getMessage}")) >>
          Stream.raiseError[IO](error)
      }
      .compile
      .drain
      .start.void
      .as(ExitCode.Success)
}
