package inc.zhugastrov.booking.db

import java.time.LocalDate

case class BookingRow(bookingId: Long, propertyId: Int, bookingDate: LocalDate)
