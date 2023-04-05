package com.rockthejvm.jobsboard.http

import cats.effect.Concurrent
import cats.{Monad, MonadThrow}
import com.rockthejvm.jobsboard.http.routes.{HealthRoutes, JobRoutes}
import org.http4s.server.Router
import cats.syntax.semigroupk.*
import org.http4s.HttpRoutes
import org.typelevel.log4cats.Logger

class HttpApi[F[_]: Concurrent: Logger] private  {
  private val healthRoutes = HealthRoutes[F].routes
  private val jobRoutes = JobRoutes[F].routes

  val endpoints: HttpRoutes[F] = Router (
    "/api" -> (healthRoutes <+> jobRoutes)
  )
}

object HttpApi {
  def apply[F[_]: Concurrent: Logger] = new HttpApi[F]
}