package inc.zhugastrov.booking.service.impl

import cats.data.EitherT
import cats.effect.{IO, Resource}
import inc.zhugastrov.booking.db.BookingRow
import inc.zhugastrov.booking.db.api.BookingDAO
import inc.zhugastrov.booking.domain.{BookingRequest, DoubleBookingResponse, PropertyReserveDatesResponse}
import inc.zhugastrov.booking.kafka.KafkaProducerService
import inc.zhugastrov.booking.service.api.BookingService
import inc.zhugastrov.booking.utils.Utils
import inc.zhugastrov.booking.utils.Utils.{ReservationId, getDatesBetween}
import io.circe.generic.auto.*
import io.circe.syntax.*


private class BookingServiceImpl private(db: BookingDAO, kafkaProducer: KafkaProducerService) extends BookingService {

  def getReservedDates(propertyId: Int): IO[PropertyReserveDatesResponse] = {
    db.getAllReservationsForPropertyId(propertyId)
      .map(l => PropertyReserveDatesResponse(propertyId, l.map(_.bookingDate).sorted))
  }

  def makeBooking(request: BookingRequest): EitherT[IO, DoubleBookingResponse, ReservationId] = {
    val datesSplit = getDatesBetween(request.from, request.to)
    for {
      batchId <- EitherT.right(db.getBatchId)
      result <- db.storeBooking(datesSplit.map(day =>
          BookingRow(batchId, request.propertyId, day))).map(_ => ReservationId(batchId))
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

object BookingServiceImpl {
  def create(db: BookingDAO, kafkaProducer: KafkaProducerService): Resource[IO, BookingService] = for {
    bookingService <- Resource.pure(BookingServiceImpl(db, kafkaProducer))
  } yield bookingService
}
