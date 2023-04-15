package com.rockthejvm.jobsboard.modules

import cats.effect.{Async, MonadCancelThrow, Resource}
import com.rockthejvm.jobsboard.core.{Jobs, LiveJobs}
import doobie.util.transactor.Transactor

final class Core[F[_]] private (val jobs: Jobs[F])

object Core {

  def apply[F[_]: Async](xa: Transactor[F]): Resource[F, Core[F]] =
    Resource
      .eval(LiveJobs[F](xa))
      .map(jobs => new Core(jobs))
}
