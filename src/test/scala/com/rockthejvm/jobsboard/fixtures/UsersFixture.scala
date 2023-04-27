package com.rockthejvm.jobsboard.fixtures

import com.rockthejvm.jobsboard.domain.user.*
import cats.implicits.*

trait UsersFixture {
  val Daniel = User(
    "daniel@rockthejvm.com",
    "rockthejvm",
    "Daniel".some,
    "Ciocirlan".some,
    "Rock The JVM".some,
    Role.ADMIN
  )

  val Riccardo = User(
    "riccardo@rockthejvm.com",
    "riccardorulez",
    "Riccardo".some,
    "Cardin".some,
    "Rock The JVM".some,
    Role.RECRUITER
  )

  val UpdatedRiccardo = User(
    "riccardo@rockthejvm.com",
    "riccardorocks",
    "RICCARDO".some,
    "CARDIN".some,
    "Adobe".some,
    Role.RECRUITER
  )

  val NewUser = User(
    "newuser@gmail.com",
    "simplepassword",
    "John".some,
    "Doe".some,
    "Company".some,
    Role.RECRUITER
  )
}
