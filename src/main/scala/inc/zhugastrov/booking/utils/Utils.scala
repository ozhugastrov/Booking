package inc.zhugastrov.booking.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object Utils {
  sealed trait BookingException
  object DoubleBookingException extends BookingException
  
  final case class ReservationId(reservationId: Long)

  private def daysBetween(from: LocalDate, to: LocalDate) = ChronoUnit.DAYS.between(from, to).toInt

  def getDatesBetween(startDate: LocalDate, endDate: LocalDate): List[LocalDate] = {
    val days = daysBetween(startDate, endDate)
    (0 until days).map(startDate.plusDays(_)).toList
  }

  def getSuggestedDates(startDate: LocalDate, endDate: LocalDate, reservedDates: List[LocalDate]): (LocalDate, LocalDate) = {
    val days = daysBetween(startDate, endDate) + 1
    val resDatesSorted = reservedDates.filter(_.isAfter(startDate)).sorted
    resDatesSorted.sliding(2).find {
        case from :: to :: Nil => daysBetween(from, to) >= days
        case _ => false
      }
      .map(l => (l.head.plusDays(1), l.head.plusDays(days))).getOrElse((resDatesSorted.last.plusDays(1), resDatesSorted.last.plusDays(days)))
  }

}
