package inc.zhugastrov.booking.utils


import inc.zhugastrov.booking.domain.BookingRequest
import io.circe.*

import java.time.LocalDate

object Encoders {

  given bookingRequestDecoder: Decoder[BookingRequest] = Decoder.instance { h =>
      for {
        propertyId <- h.get[Int]("propertyId")
        startDate <- h.get[LocalDate]("startDate")
        endDate <- h.get[LocalDate]("endDate")
      } yield BookingRequest(propertyId, startDate, endDate)
    }

}
