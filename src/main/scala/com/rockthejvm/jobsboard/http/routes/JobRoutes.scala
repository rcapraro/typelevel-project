package com.rockthejvm.jobsboard.http.routes

import cats.effect.Concurrent
import cats.implicits.*
import cats.{Monad, MonadThrow}
import com.rockthejvm.jobsboard.core.Jobs
import com.rockthejvm.jobsboard.domain.job.*
import com.rockthejvm.jobsboard.domain.pagination.*
import com.rockthejvm.jobsboard.http.responses.FailureResponse
import com.rockthejvm.jobsboard.http.validation.syntax.*
import com.rockthejvm.jobsboard.logging.syntax.*
import io.circe.generic.auto.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

import java.util.UUID
import scala.collection.mutable
import com.rockthejvm.jobsboard.domain.job.JobFilter

class JobRoutes[F[_]: Concurrent: Logger] private (jobs: Jobs[F]) extends Http4sDsl[F] with HttpValidationDsl[F] {

  object OffsetQueryParam extends OptionalQueryParamDecoderMatcher[Int]("offset")
  object LimitQueryParam  extends OptionalQueryParamDecoderMatcher[Int]("limit")

  // GET /jobs?limit=x&offset=y { filters }
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root :? LimitQueryParam(limit) +& OffsetQueryParam(offset) =>
      for {
        filter  <- req.as[JobFilter]
        jobList <- jobs.all(filter, Pagination(limit, offset))
        resp    <- Ok(jobList)
      } yield resp
  }

  // GET /jobs/uuid
  private val findJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    jobs.find(id).flatMap {
      case Some(job) => Ok(job)
      case None      => NotFound(FailureResponse(s"Job with $id not found."))
    }
  }

  // POST /jobs {jobInfo}
  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "create" =>
    req.validate[JobInfo] { jobInfo =>
      for {
        jobId <- jobs.create("TODO@rockthejvm.com", jobInfo)
        resp  <- Created(jobId)
      } yield resp
    }
  }

  // PUT /jobs/uuid/ {jobInfo}
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ PUT -> Root / UUIDVar(id) =>
    req.validate[JobInfo] { jobInfo =>
      for {
        maybeNewJob <- jobs.update(id, jobInfo)
        resp <- maybeNewJob match {
          case Some(_) => Ok()
          case None    => NotFound(FailureResponse(s"Cannot update job with id $id: not found."))
        }
      } yield resp
    }
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case DELETE -> Root / UUIDVar(id) =>
    jobs.find(id).flatMap {
      case Some(_) =>
        for {
          _    <- jobs.delete(id)
          resp <- NoContent()
        } yield resp
      case None => NotFound(FailureResponse(s"Cannot delete job with id $id: not found."))
    }
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (allJobsRoute <+> findJobsRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )
}

object JobRoutes {
  def apply[F[_]: Concurrent: Logger](jobs: Jobs[F]) = new JobRoutes[F](jobs)
}
