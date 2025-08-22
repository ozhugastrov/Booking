package inc.zhugastrov.booking.utils.validation

import inc.zhugastrov.booking.domain.BookingRequest
import inc.zhugastrov.booking.utils.validation.api.{Success, ValidationsError, Validator}

import java.time.LocalDate

object Validators {
  val bookingRequestValidator: Validator[BookingRequest] = (r: BookingRequest) => {
    val isFromInPast = if (r.from.isBefore(LocalDate.now())) List(s"Start date can't be before today") else List()
    val isFromBeforeTo = if (!r.to.isAfter(r.from)) List(s"End date ${r.to} must me after start date") else List()

    val errors = isFromInPast ++ isFromBeforeTo

    if (errors.isEmpty) Success else ValidationsError(errors)
  }

}
