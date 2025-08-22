package inc.zhugastrov.booking.domain

import java.time.LocalDate

case class DoubleBookingResponse(bookingId: Long, propertyId: Int, actualFrom: LocalDate, actualTo: LocalDate,
                                 suggestedFrom: LocalDate, suggestedTo: LocalDate) 

