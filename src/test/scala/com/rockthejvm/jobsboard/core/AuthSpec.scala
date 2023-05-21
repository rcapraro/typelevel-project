package com.rockthejvm.jobsboard.core

import cats.data.OptionT
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.*
import com.rockthejvm.jobsboard.domain.auth.*
import com.rockthejvm.jobsboard.domain.security.*
import com.rockthejvm.jobsboard.domain.user.*
import com.rockthejvm.jobsboard.fixtures.*
import org.scalatest.Inside
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tsec.authentication.{IdentityStore, JWTAuthenticator}
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.duration.*

class AuthSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with UsersFixture {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private val mockedUsers: Users[IO] = new Users[IO]:
    override def find(email: String): IO[Option[User]] =
      if email == DanielEmail then IO.pure(Some(Daniel)) else IO.pure(None)

    override def create(user: User): IO[String] = IO.pure(user.email)

    override def update(user: User): IO[Option[User]] = IO.pure(Some(user))

    override def delete(email: String): IO[Boolean] = IO.pure(true)

  private val mockedAuthenticator: Authenticator[IO] = {
    // key for hashing
    val key = HMACSHA256.unsafeGenerateKey
    // identity store to retrieve users
    val idStore: IdentityStore[IO, String, User] = (email: String) =>
      if email == DanielEmail then OptionT.pure(Daniel)
      else if email == RiccardoEmail then OptionT.pure(Riccardo)
      else OptionT.none[IO, User]
    // jwt authenticator
    JWTAuthenticator.unbacked.inBearerToken(
      1.day, // token expiration
      None,  // max idle time
      idStore,
      key
    )
  }

  "Auth 'algebra'" - {
    "login should return None if the user does not exist" in {
      val program = for {
        auth       <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login("user@rockthejvm.com", "password")
      } yield maybeToken

      program.asserting(_ shouldBe None)
    }

    "login should return None if the user exists but the password is wrong" in {
      val program = for {
        auth       <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login(DanielEmail, "wrongpassword")
      } yield maybeToken

      program.asserting(_ shouldBe None)
    }

    "login should return a token if the user exists and the password is correct" in {
      val program = for {
        auth       <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login(DanielEmail, "rockthejvm")
      } yield maybeToken

      program.asserting(_ shouldBe defined)
    }

    "signing up should should not create a user with an existing email" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeUser <- auth.signup(
          NewUserInfo(DanielEmail, "somePassword", Some("Daniel"), Some("Whatever"), Some("Other company"))
        )
      } yield maybeUser

      program.asserting(_ shouldBe None)
    }

    "signing up should should create a completely new user" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeUser <- auth.signup(
          NewUserInfo("bob@rockthejvm.com", "somePassword", Some("Bob"), Some("Jones"), Some("Company"))
        )
      } yield maybeUser

      program.asserting {
        case Some(user) =>
          user.email shouldBe "bob@rockthejvm.com"
          user.firstName shouldBe Some("Bob")
          user.lastName shouldBe Some("Jones")
          user.company shouldBe Some("Company")
          user.role shouldBe Role.RECRUITER
        case None => fail()
      }
    }

    "changePassword should return Right(None) if the user does not exist" in {
      val program = for {
        auth   <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        result <- auth.changePassword("alice@rockthejvm.com", NewPasswordInfo("oldPassword", "newPassword"))
      } yield result

      program.asserting(_ shouldBe Right(None))
    }

    "changePassword should return Left with an error if the user exists and the password is wrong" in {
      val program = for {
        auth   <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        result <- auth.changePassword(DanielEmail, NewPasswordInfo("oldPassword", "newPassword"))
      } yield result

      program.asserting(_ shouldBe Left("Invalid password"))
    }

    "changePassword should correctly change password if all details are correct" in {
      val program = for {
        auth   <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        result <- auth.changePassword(DanielEmail, NewPasswordInfo("rockthejvm", "scalarocks"))
        isNicePassword <- result match
          case Right(Some(user)) => BCrypt.checkpwBool[IO]("scalarocks", PasswordHash[BCrypt](user.hashedPassword))
          case _                 => IO.pure(false)
      } yield isNicePassword

      program.asserting(_ shouldBe true)
    }

  }

}
