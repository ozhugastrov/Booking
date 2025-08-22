package inc.zhugastrov.booking.utils.validation.api

sealed trait  ValidationResult 

object Success extends ValidationResult

final case class ValidationsError(errors: List[String]) extends ValidationResult


