package inc.zhugastrov.booking.db

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import inc.zhugastrov.booking.domain.Booking
import inc.zhugastrov.booking.utils.Utils.{BookingException, DoubleBookingException}

import java.time.LocalDate


class BookingDAO private {

  private val xa = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql:testdb",
    user = "postgres",
    password = "zhutest",
    logHandler = None
  )

  private val createTableSql =
    sql"""
          CREATE TABLE IF NOT EXISTS bookings (
          id SERIAL PRIMARY KEY,
          booking_id BIGINT NOT NULL,
          property_id INT NOT NULL,
          booking_date DATE NOT NULL,
          UNIQUE (property_id, booking_date))
        """

  private val createTableProgram: ConnectionIO[Int] = createTableSql.update.run

  private val createSequenceSql =
    sql"""CREATE SEQUENCE IF NOT EXISTS batch_id_seq
         """

  private def asdf() = ""
  
  private val createSequenceProgram = createSequenceSql.update.run

  def getAllReservationsForPropertyId(propertyId: Int): IO[List[Booking]] = {
    sql"select property_id, booking_date from bookings"
      .query[Booking]
      .to[List]
      .transact(xa)
  }

  def getBatchId: IO[Long] = {
    sql"select nextval('batch_id_seq')".query[Long].unique.transact(xa)
  }


  def storeBooking(booking: List[BookingRow]): IO[Either[BookingException, Int]] = {
    val sql = "insert into bookings (booking_id, property_id, booking_date) values (?, ?, ?)"
    Update[BookingRow](sql).updateMany(booking).attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => DoubleBookingException
      }
      .transact(xa)
  }

  def run: IO[Unit] = for {
    _ <- createTableProgram.transact(xa).void
    _ <- createSequenceProgram.transact(xa).void
    _ <- IO.println("Creating sample bookings…")
    batchId <- getBatchId
    bookings = List(
      BookingRow(batchId, 123, LocalDate.parse("2025-05-11")),
      BookingRow(batchId, 123, LocalDate.parse("2025-05-12")),
      BookingRow(batchId, 123, LocalDate.parse("2025-05-13"))
    )
    // Choose one of these:
    insertedCount <- storeBooking(bookings)
    // rows <- storeBookingsReturning(bookings)
    _ <- IO.println(s"Inserted rows: $insertedCount")
    fetched <- getAllReservationsForPropertyId(123)
    _ <- IO.println(s"Fetched back: $fetched")
  } yield ()

}

object BookingDAO {
  def create: IO[BookingDAO] = for {
    bookingService <- IO.pure(BookingDAO())
    _ <- bookingService.createTableProgram.transact(bookingService.xa).void
    _ <- bookingService.createSequenceProgram.transact(bookingService.xa).void 
  } yield bookingService
}
