package inc.zhugastrov.booking.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object Utils {
  sealed trait BookingException

  object DoubleBookingException extends BookingException

  private def daysBetween(from: LocalDate, to: LocalDate) = ChronoUnit.DAYS.between(from, to).toInt

  def getDatesBetween(startDate: LocalDate, endDate: LocalDate): List[LocalDate] = {
    val days = daysBetween(startDate, endDate)
    (0 until days).map(startDate.plusDays(_)).toList
  }

  def getSuggestedDates(startDate: LocalDate, endDate: LocalDate, reservedDates: List[LocalDate]): (LocalDate, LocalDate) = {
    val days = ChronoUnit.DAYS.between(startDate, endDate).toInt
    val resDatesSorted = reservedDates.sorted
    resDatesSorted.sliding(2).find {
        case from :: to :: Nil => daysBetween(startDate, endDate) >= days
        case _ => false
      }
      .map(l => (l.head, l.head.plusDays(days))).getOrElse((resDatesSorted.last, resDatesSorted.last.plusDays(days)))
  }

}
