package inc.zhugastrov.booking.service

import cats.data.EitherT
import cats.effect.IO
import inc.zhugastrov.booking.db.{BookingDAO, BookingRow}
import inc.zhugastrov.booking.domain.{BookingRequest, PropertyReserveDatesResponse}
import inc.zhugastrov.booking.kafka.KafkaProducerService
import inc.zhugastrov.booking.utils.Utils
import inc.zhugastrov.booking.utils.Utils.{DoubleBookingException, getDatesBetween}

class BookingService(db: BookingDAO, kafkaProducer: KafkaProducerService) {

  def getReservedDates(propertyId: Int): IO[PropertyReserveDatesResponse] = {
    db.getAllReservationsForPropertyId(propertyId)
      .map(l => PropertyReserveDatesResponse(propertyId, l.map(_.bookingDate).sorted))
  }

  def makeBooking(request: BookingRequest): EitherT[IO, Utils.BookingException, Int] = {
    val datesSplit = getDatesBetween(request.from, request.to)
    for {
      batchId <- EitherT.right(db.getBatchId)
      result <- EitherT(db.storeBooking(datesSplit.map(day =>
        BookingRow(batchId, request.propertyId, day))))
        .leftSemiflatMap(be => kafkaProducer.produce(batchId, "bookingException") *> IO.pure(be))
    } yield result
  }
}
