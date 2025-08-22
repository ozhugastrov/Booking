package inc.zhugastrov.booking.db

import cats.effect.IO
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux

object Dependencies {
   val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql:testdb",
    user = "postgres",
    password = "zhutest",
    logHandler = None
  )
}
