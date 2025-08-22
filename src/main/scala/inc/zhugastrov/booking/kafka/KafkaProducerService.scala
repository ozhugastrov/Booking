package inc.zhugastrov.booking.kafka

import cats.effect.IO
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerResult, ProducerSettings}


class KafkaProducerService {

  private val producerSettings: ProducerSettings[IO, Long, String] =
    ProducerSettings[IO, Long, String]
      .withBootstrapServers("localhost:9092")

  def produce(key: Long, value: String): IO[ProducerResult[Long, String]] = {
    KafkaProducer.resource(producerSettings).use { producer =>
      val record = ProducerRecord("booking", key, value)
      producer.produce(ProducerRecords.one(record)).flatten
    }
  }


}
