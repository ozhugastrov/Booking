package inc.zhugastrov.booking.db.api

import cats.data.EitherT
import cats.effect.IO
import inc.zhugastrov.booking.db.BookingRow
import inc.zhugastrov.booking.domain.{Booking, DoubleBookingResponse}
import inc.zhugastrov.booking.utils.Utils.BookingException

trait BookingDAO {
  def getAllReservationsForPropertyId(propertyId: Int): IO[List[Booking]]

  def getBatchId: IO[Long]

  def storeBookingConflict(bookingConflict: DoubleBookingResponse): IO[Int]

  def storeBooking(booking: List[BookingRow]): EitherT[IO, BookingException, Int]
  
}
