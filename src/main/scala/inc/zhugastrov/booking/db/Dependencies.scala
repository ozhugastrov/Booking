package inc.zhugastrov.booking.db

import cats.effect.IO
import doobie.Transactor

object Dependencies {
   val xa = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql:testdb",
    user = "postgres",
    password = "zhutest",
    logHandler = None
  )
}
