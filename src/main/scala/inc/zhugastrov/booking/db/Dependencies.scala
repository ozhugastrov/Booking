package inc.zhugastrov.booking.db

import cats.effect.IO
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux
import inc.zhugastrov.booking.config.AppConfig

object Dependencies {
  def xa(config: AppConfig): Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = config.postgresUrl,
    user = config.postgresName,
    password = config.postgresPass.value,
    logHandler = None
  )
}
