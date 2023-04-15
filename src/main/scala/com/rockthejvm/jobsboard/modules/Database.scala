package com.rockthejvm.jobsboard.modules

import cats.effect.{Async, Resource}
import com.rockthejvm.jobsboard.config.PostgresConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Database {

  def makePostgresResource[F[_]: Async](config: PostgresConfig): Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool(config.nThreads)
    xa <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      config.url,
      config.user,
      config.pass,
      ec
    )
  } yield xa

}
