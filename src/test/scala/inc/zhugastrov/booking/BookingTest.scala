package inc.zhugastrov.booking

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux
import inc.zhugastrov.booking.db.BookingRow
import inc.zhugastrov.booking.db.impl.BookingDAOImpl
import inc.zhugastrov.booking.domain.BookingRequest
import inc.zhugastrov.booking.kafka.KafkaProducerService
import inc.zhugastrov.booking.service.impl.BookingServiceImpl
import inc.zhugastrov.booking.utils.Utils
import io.github.embeddedkafka.Codecs.stringDeserializer
import io.github.embeddedkafka.EmbeddedKafka
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.testcontainers.containers.PostgreSQLContainer

import java.time.LocalDate

class BookingTest extends AnyFlatSpec with BeforeAndAfterAll with should.Matchers with EmbeddedKafka {

  val postgres = new PostgreSQLContainer(
    "postgres:16-alpine"
  )

  override def beforeAll(): Unit = {
    postgres.start()
  }

  "read after write" should "be 3" in {
    val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = postgres.getJdbcUrl,
      user = postgres.getUsername,
      password = postgres.getPassword,
      logHandler = None
    )


    val storeRead = for {
      db <- BookingDAOImpl.create(xa)
      store <- Resource.eval(db.storeBooking(List(
        BookingRow(123L, 123, LocalDate.parse("2025-08-10")),
        BookingRow(123L, 123, LocalDate.parse("2025-08-11")),
        BookingRow(123L, 123, LocalDate.parse("2025-08-12")))
      ).value)
      read <- Resource.eval(db.getAllReservationsForPropertyId(123))
    } yield read

    storeRead.use(IO.pure).unsafeRunSync().length should be(3)
  }

  "booking service" should "send errors to kafka" in {
    withRunningKafka {

      createCustomTopic("booking")

      val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
        driver = "org.postgresql.Driver",
        url = postgres.getJdbcUrl,
        user = postgres.getUsername,
        password = postgres.getPassword,
        logHandler = None
      )

      val result = for {
        producer <- Resource.pure(KafkaProducerService("localhost:6001"))
        db <- BookingDAOImpl.create(xa)
        bookService <- BookingServiceImpl.create(db, producer)
        res <- Resource.eval(bookService.makeBooking(BookingRequest(123, LocalDate.parse("2025-08-05"), LocalDate.parse("2025-08-10"))).value &>
          bookService.makeBooking(BookingRequest(123, LocalDate.parse("2025-08-05"), LocalDate.parse("2025-08-10"))).value)
      } yield res

      result.use(IO.pure).unsafeRunSync()
      consumeFirstMessageFrom("booking") should not be empty
    }
  }

  "numbers of days" should "be 6" in {
    Utils.getDatesBetween(LocalDate.parse("2025-08-04"), LocalDate.parse("2025-08-10")).length should be(6)
  }

  "suggested days" should "be 2025-08-08 and 2025-08-11" in {
    Utils.getSuggestedDates(LocalDate.parse("2025-08-04"), LocalDate.parse("2025-08-07"),
      List(LocalDate.parse("2025-08-07"))) should be((LocalDate.parse("2025-08-08"), LocalDate.parse("2025-08-11")))
  }


}
