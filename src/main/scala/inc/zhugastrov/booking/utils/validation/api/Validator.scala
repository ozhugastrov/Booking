package inc.zhugastrov.booking.utils.validation.api

trait Validator[T] {
  def validate(target: T): ValidationResult
}
