package inc.zhugastrov.booking.domain

import java.time.LocalDate

case class PropertyReserveDatesResponse(propertyId: Int, reserveDates: List[LocalDate])
