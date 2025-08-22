package inc.zhugastrov.booking.routes

import cats.effect.{Async, IO}
import inc.zhugastrov.booking.domain.BookingRequest
import inc.zhugastrov.booking.service.BookingService
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.circe.jsonEncoder
import org.http4s.circe.CirceEntityDecoder.*
import inc.zhugastrov.booking.utils.Encoders.bookingRequestDecoder
import inc.zhugastrov.booking.utils.Utils.DoubleBookingException
import inc.zhugastrov.booking.utils.validation.Validators.bookingRequestValidator
import inc.zhugastrov.booking.utils.validation.api.{Success, ValidationsError}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder


object BookingRouts {

  def bookingRoutes(service: BookingService): HttpRoutes[IO] = {

    val dsl = Http4sDsl[IO]
    import dsl.*
    HttpRoutes.of[IO] {
      case GET -> Root / bookings / IntVar(id) =>
        service.getReservedDates(id).flatMap(r => Ok(r.asJson))
      case request@POST -> Root / bookings / book =>
        request.as[BookingRequest].flatMap(br => {
          bookingRequestValidator.validate(br) match {
            case ValidationsError(errors) => BadRequest(errors)
            case Success => service.makeBooking(br).flatMap {
              case Left(DoubleBookingException) => Conflict("")
              case Right(value) => Ok("")
            }
          }
        }
        )
    }
  }

  //  private def getReservationsForId

}
