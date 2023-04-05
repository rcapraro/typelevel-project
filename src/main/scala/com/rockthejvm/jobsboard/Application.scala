package com.rockthejvm.jobsboard

import cats.Monad
import cats.effect.{IO, IOApp}
import com.rockthejvm.jobsboard.config.EmberConfig
import com.rockthejvm.jobsboard.http.routes.HealthRoutes
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import pureconfig.ConfigReader.Result
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
import com.rockthejvm.jobsboard.config.Syntax.loadF
import cats.syntax.flatMap.*
import com.rockthejvm.jobsboard.http.HttpApi
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Application extends IOApp.Simple{

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = ConfigSource.default.loadF[IO, EmberConfig].flatMap{config =>
    EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(HttpApi[IO].endpoints.orNotFound)
      .build
      .use(_ => IO.println("Server ready!") *> IO.never)
  }

}
