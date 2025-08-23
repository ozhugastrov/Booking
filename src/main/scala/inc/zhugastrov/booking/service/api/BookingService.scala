package inc.zhugastrov.booking.service.api

import cats.data.EitherT
import cats.effect.IO
import inc.zhugastrov.booking.domain.{BookingRequest, DoubleBookingResponse, PropertyReserveDatesResponse}
import inc.zhugastrov.booking.utils.Utils.ReservationId

trait BookingService {

  def getReservedDates(propertyId: Int): IO[PropertyReserveDatesResponse]
  def makeBooking(request: BookingRequest): EitherT[IO, DoubleBookingResponse, ReservationId]

}
