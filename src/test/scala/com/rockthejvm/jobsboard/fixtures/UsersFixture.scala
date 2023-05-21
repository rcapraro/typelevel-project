package com.rockthejvm.jobsboard.fixtures

import com.rockthejvm.jobsboard.domain.user.*
import cats.implicits.*

/*
rockthejvm => $2a$10$ljy9SV0to0CZTCaRH2QAHegl39SDMlBKDTsGVYSp6loZWLxQFqLNa
riccardorulez => $2a$10$sKvbhH4Bi3XjlQwxOW8rV.I7JfG5nva1AuDq/k0DvG/CLf2YqExYy
riccardorocks => $2a$10$ruHDSHP/.RxUrDdGA633VucZq8l7RHq3uaRvcME31UbTQlIsmCcfG
simplepassword => $2a$10$kF10D0FVxTitUHHh8uZ/9el14nrLSfEU4mtWaMJfZ3YRRXiDXH4L.
 */

trait UsersFixture {
  val Daniel: User = User(
    "daniel@rockthejvm.com",
    "$2a$10$ljy9SV0to0CZTCaRH2QAHegl39SDMlBKDTsGVYSp6loZWLxQFqLNa",
    "Daniel".some,
    "Ciocirlan".some,
    "Rock The JVM".some,
    Role.ADMIN
  )

  val DanielEmail: String = Daniel.email

  val Riccardo: User = User(
    "riccardo@rockthejvm.com",
    "$2a$10$sKvbhH4Bi3XjlQwxOW8rV.I7JfG5nva1AuDq/k0DvG/CLf2YqExYy",
    "Riccardo".some,
    "Cardin".some,
    "Rock The JVM".some,
    Role.RECRUITER
  )

  val RiccardoEmail: String = Daniel.email

  val UpdatedRiccardo: User = User(
    "riccardo@rockthejvm.com",
    "$2a$10$ruHDSHP/.RxUrDdGA633VucZq8l7RHq3uaRvcME31UbTQlIsmCcfG",
    "RICCARDO".some,
    "CARDIN".some,
    "Adobe".some,
    Role.RECRUITER
  )

  val NewUser: User = User(
    "newuser@gmail.com",
    "$2a$10$kF10D0FVxTitUHHh8uZ/9el14nrLSfEU4mtWaMJfZ3YRRXiDXH4L.",
    "John".some,
    "Doe".some,
    "Company".some,
    Role.RECRUITER
  )
}
