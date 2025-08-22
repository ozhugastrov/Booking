package inc.zhugastrov.booking.service

import cats.effect.IO
import inc.zhugastrov.booking.db.{BookingDAO, BookingRow}
import inc.zhugastrov.booking.domain.{BookingRequest, PropertyReserveDatesResponse}
import inc.zhugastrov.booking.utils.Utils
import inc.zhugastrov.booking.utils.Utils.getDatesBetween

class BookingService(db: BookingDAO) {

  def getReservedDates(propertyId: Int): IO[PropertyReserveDatesResponse] = {
    db.getAllReservationsForPropertyId(propertyId)
      .map(l => PropertyReserveDatesResponse(propertyId, l.map(_.bookingDate).sorted))
  }

  def makeBooking(request: BookingRequest): IO[Either[Utils.BookingException, Int]] = {
    val datesSplit = getDatesBetween(request.from, request.to)
    for {
      batchId <- db.getBatchId
      result <- db.storeBooking(datesSplit.map(day => BookingRow(batchId, request.propertyId, day)))
    } yield result
  }
}
