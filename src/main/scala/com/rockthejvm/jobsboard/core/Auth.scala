package com.rockthejvm.jobsboard.core

import cats.effect.*
import cats.implicits.*
import com.rockthejvm.jobsboard.domain.auth.*
import com.rockthejvm.jobsboard.domain.security.*
import com.rockthejvm.jobsboard.domain.user.*
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.Logger
import tsec.authentication.{AugmentedJWT, JWTAuthenticator}
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

trait Auth[F[_]] {
  def login(email: String, password: String): F[Option[JwtToken]]
  def signup(newUserInfo: NewUserInfo): F[Option[User]]
  def changePassword(email: String, newPasswordInfo: NewPasswordInfo): F[Either[String, Option[User]]]
}

class LiveAuth[F[_]: Async: Logger] private (users: Users[F], authenticator: Authenticator[F]) extends Auth[F] {
  override def login(email: String, password: String): F[Option[JwtToken]] =
    for {
      // find the user in the database and return None if no user
      maybeUser <- users.find(email)
      // check password
      maybeValidatedUser <- maybeUser.filterA(user =>
        BCrypt.checkpwBool[F](password, PasswordHash[BCrypt](user.hashedPassword))
      )
      // return a new token if password matches
      maybeJwtToken <- maybeValidatedUser.traverse(user => authenticator.create(user.email))
    } yield maybeJwtToken

  override def signup(newUserInfo: NewUserInfo): F[Option[User]] =
    // find the use in the db, iw we did => None
    users.find(newUserInfo.email).flatMap {
      case Some(_) => None.pure[F]
      case None =>
        for {
          // hash the new password
          hashedPassword <- BCrypt.hashpw[F](newUserInfo.password)
          // create a new user in the db
          user <- User(
            newUserInfo.email,
            hashedPassword,
            newUserInfo.firstName,
            newUserInfo.lastName,
            newUserInfo.company,
            Role.RECRUITER
          ).pure[F]
          _ <- users.create(user)
        } yield Some(user)
    }

  override def changePassword(email: String, newPasswordInfo: NewPasswordInfo): F[Either[String, Option[User]]] = {

    def updateUser(user: User, newPassword: String): F[Option[User]] =
      for {
        hashedPassword <- BCrypt.hashpw[F](newPassword)
        updatedUser    <- users.update(user.copy(hashedPassword = hashedPassword))
      } yield updatedUser

    def checkAndUpdate(user: User, oldPassword: String, newPassword: String): F[Either[String, Option[User]]] =
      for {
        // check password
        passCheck <- BCrypt.checkpwBool[F](oldPassword, PasswordHash[BCrypt](user.hashedPassword))
        // if password ok, hash new password and update user
        updateResult <-
          if (passCheck) updateUser(user, newPassword).map(Right(_))
          else Left("Invalid password").pure[F]
      } yield updateResult

    users.find(email).flatMap {
      case None => Right(None).pure[F]
      case Some(user) =>
        val NewPasswordInfo(oldPassword, newPassword) = newPasswordInfo
        checkAndUpdate(user, oldPassword, newPassword)
    }

  }

}

object LiveAuth {
  def apply[F[_]: Async: Logger](users: Users[F], authenticator: Authenticator[F]): F[LiveAuth[F]] =
    new LiveAuth[F](users, authenticator).pure[F]
}
