package com.rockthejvm.jobsboard.core

import cats.effect.*
import cats.implicits.*
import org.typelevel.log4cats.Logger
import tsec.authentication.AugmentedJWT
import tsec.mac.jca.HMACSHA256

import com.rockthejvm.jobsboard.domain.security.*
import com.rockthejvm.jobsboard.domain.auth.*
import com.rockthejvm.jobsboard.domain.user.*
import org.http4s.server.middleware.Logger
import tsec.authentication.JWTAuthenticator

trait Auth[F[_]] {
  def login(email: String, password: String): F[Option[JwtToken]]
  def signup(newUserInfo: NewUserInfo): F[Option[User]]
  def changePassword(email: String, newPasswordInfo: NewPasswordInfo): F[Either[String, User]]
}

class LiveAuth[F[_]: MonadCancelThrow: Logger] private (users: Users[F], authenticator: Authenticator[F]) extends Auth[F] {
  override def login(email: String, password: String): F[Option[JwtToken]]                              = ???
  override def signup(newUserInfo: NewUserInfo): F[Option[User]]                                        = ???
  override def changePassword(email: String, newPasswordInfo: NewPasswordInfo): F[Either[String, User]] = ???
}
object LiveAuth {
  def apply[F[_]: MonadCancelThrow: Logger](users: Users[F], authenticator: Authenticator[F]): F[LiveAuth[F]] = new LiveAuth[F](users, authenticator).pure[F]
}
