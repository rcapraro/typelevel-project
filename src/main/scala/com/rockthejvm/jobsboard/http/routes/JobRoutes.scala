package com.rockthejvm.jobsboard.http.routes

import cats.effect.Concurrent
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.semigroupk.*
import cats.{Monad, MonadThrow}
import com.rockthejvm.jobsboard.domain.job.{Job, JobInfo}
import com.rockthejvm.jobsboard.http.responses.FailureResponse
import com.rockthejvm.jobsboard.logging.syntax.*
import io.circe.generic.auto.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

import java.util.UUID
import scala.collection.mutable

class JobRoutes[F[_]: Concurrent: Logger] private extends Http4sDsl[F] {

  // database
  private val database = mutable.Map[UUID, Job]()

  // HRy /jobs?offset=x&limit=y { filters }
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok(database.values)
  }

  // GET /jobs/uuid
  private val findJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    database.get(id) match
      case Some(job) => Ok(job)
      case None      => NotFound(FailureResponse(s"Job with $id not found."))
  }

  // POST /jobs {jobInfo}
  private def createJob(jobInfo: JobInfo): F[Job] =
    Job(id = UUID.randomUUID(), date = System.currentTimeMillis(), jobInfo = jobInfo, ownerEmail = "todo@rockthejvm.com", active = true).pure[F]

  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root =>
    for {
      jobInfo <- req.as[JobInfo].logError(e => s"Parsing payload failed: $e")
      job     <- createJob(jobInfo)
      _       <- database.put(job.id, job).pure[F]
      resp    <- Created(job.id)
    } yield resp
  }

  // PUT /jobs/uuid/ {jobInfo}
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ PUT -> Root / UUIDVar(id) =>
    database.get(id) match
      case Some(job) =>
        for {
          jobInfo <- req.as[JobInfo]
          _       <- database.put(id, job.copy(jobInfo = jobInfo)).pure[F]
          resp    <- Ok()
        } yield resp
      case None => NotFound(FailureResponse(s"Cannot update job with id $id: not found."))
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case DELETE -> Root / UUIDVar(id) =>
    database.get(id) match
      case Some(_) =>
        for {
          _    <- database.remove(id).pure[F]
          resp <- NoContent()
        } yield resp
      case None => NotFound(FailureResponse(s"Cannot delete job with id $id: not found."))
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (allJobsRoute <+> findJobsRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )
}

object JobRoutes {
  def apply[F[_]: Concurrent: Logger] = new JobRoutes[F]
}
