package inc.zhugastrov.booking.domain

import inc.zhugastrov.booking.utils.validation.api.Success

import java.time.LocalDate

case class BookingRequest(propertyId: Int, from: LocalDate, to: LocalDate)