package inc.zhugastrov.booking.db.impl

import cats.data.EitherT
import cats.effect.*
import cats.effect.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import inc.zhugastrov.booking.db.BookingRow
import inc.zhugastrov.booking.db.api.BookingDAO
import inc.zhugastrov.booking.domain.{Booking, DoubleBookingResponse}
import inc.zhugastrov.booking.utils.Utils.{BookingException, DoubleBookingException}

import java.time.LocalDate


private class BookingDAOImpl private(val xa: Aux[IO, Unit]) extends BookingDAO {

  private val createBookingConflictsSql =
    sql"""
            CREATE TABLE IF NOT EXISTS booking_conflicts (
            booking_id BIGINT NOT NULL,
            property_id INT NOT NULL,
            actual_booking_from DATE NOT NULL,
            actual_booking_to DATE NOT NULL,
            suggested_booking_from DATE NOT NULL,
            suggested_booking_to DATE NOT NULL)
          """

  private val createBookingConflictsProgram: ConnectionIO[Int] = createBookingConflictsSql.update.run


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

  private val createSequenceProgram = createSequenceSql.update.run

  def getAllReservationsForPropertyId(propertyId: Int): IO[List[Booking]] = {
    sql"select property_id, booking_date from bookings where property_id = $propertyId"
      .query[Booking]
      .to[List]
      .transact(xa)
  }

  def getBatchId: IO[Long] = {
    sql"select nextval('batch_id_seq')".query[Long].unique.transact(xa)
  }

  def storeBookingConflict(bookingConflict: DoubleBookingResponse): IO[Int] = {
    sql"""insert into booking_conflicts (booking_id, property_id, actual_booking_from, actual_booking_to, suggested_booking_from, suggested_booking_to)
         values (${bookingConflict.bookingId}, ${bookingConflict.propertyId}, ${bookingConflict.actualFrom},
          ${bookingConflict.actualTo}, ${bookingConflict.suggestedFrom}, ${bookingConflict.suggestedTo})"""
      .update.run.transact(xa)
  }


  def storeBooking(booking: List[BookingRow]): EitherT[IO, BookingException, Int] = {
    val sql = "insert into bookings (booking_id, property_id, booking_date) values (?, ?, ?)"
    EitherT(Update[BookingRow](sql).updateMany(booking).attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => DoubleBookingException
      }
      .transact(xa))
  }

}

object BookingDAOImpl {
  def create(xa: Aux[IO, Unit]): Resource[IO, BookingDAO] = for {
    bookingDAO <- Resource.pure(BookingDAOImpl(xa))
    _ <- bookingDAO.createTableProgram.transact(bookingDAO.xa).void.toResource
    _ <- bookingDAO.createBookingConflictsProgram.transact(bookingDAO.xa).void.toResource
    _ <- bookingDAO.createSequenceProgram.transact(bookingDAO.xa).void.toResource
  } yield bookingDAO
}
