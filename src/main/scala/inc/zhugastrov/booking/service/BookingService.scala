package inc.zhugastrov.booking.service

import cats.data.EitherT
import cats.effect.IO
import inc.zhugastrov.booking.db.{BookingDAO, BookingRow}
import inc.zhugastrov.booking.domain.{BookingRequest, DoubleBookingResponse, PropertyReserveDatesResponse}
import inc.zhugastrov.booking.kafka.KafkaProducerService
import inc.zhugastrov.booking.utils.Utils
import inc.zhugastrov.booking.utils.Utils.getDatesBetween
import io.circe.generic.auto.*
import io.circe.syntax.*


class BookingService(db: BookingDAO, kafkaProducer: KafkaProducerService) {

  def getReservedDates(propertyId: Int): IO[PropertyReserveDatesResponse] = {
    db.getAllReservationsForPropertyId(propertyId)
      .map(l => PropertyReserveDatesResponse(propertyId, l.map(_.bookingDate).sorted))
  }

  def makeBooking(request: BookingRequest): EitherT[IO, DoubleBookingResponse, Int] = {
    val datesSplit = getDatesBetween(request.from, request.to)
    for {
      batchId <- EitherT.right(db.getBatchId)
      result <- EitherT(db.storeBooking(datesSplit.map(day =>
        BookingRow(batchId, request.propertyId, day))))
        .leftSemiflatMap(be => {
          db.getAllReservationsForPropertyId(request.propertyId)
            .map(resDates =>
              Utils.getSuggestedDates(request.from, request.to, resDates.map(_.bookingDate)))
            .flatMap((sugFrom, sugTo) =>
              val dbr = DoubleBookingResponse(batchId, request.propertyId, request.from, request.to, sugFrom, sugTo)
              kafkaProducer.produce(batchId, dbr.asJson.noSpaces)
            *> IO.pure(dbr)
          )
        })
    } yield result
  }
}
