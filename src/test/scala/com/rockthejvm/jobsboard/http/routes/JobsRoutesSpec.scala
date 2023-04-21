package com.rockthejvm.jobsboard.http.routes

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.rockthejvm.jobsboard.core.Jobs
import com.rockthejvm.jobsboard.domain.job.*
import com.rockthejvm.jobsboard.fixtures.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.*
import org.http4s.implicits.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID

class JobsRoutesSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Http4sDsl[IO] with JobFixture {

  // prep
  val jobs: Jobs[IO] = new Jobs[IO] {
    override def create(ownerEmail: String, jobInfo: JobInfo): IO[UUID] = IO.pure(NewJobUuid)

    override def all(): IO[List[Job]] = IO.pure(List(AwesomeJob))

    override def find(id: UUID): IO[Option[Job]] =
      if (id == AwesomeJobUuid)
        IO.pure(Some(AwesomeJob))
      else
        IO.pure(None)

    override def update(id: UUID, jobInfo: JobInfo): IO[Option[Job]] =
      if (id == AwesomeJobUuid)
        IO.pure(Some(UpdatedAwesomeJob))
      else
        IO.pure(None)

    override def delete(id: UUID): IO[Int] =
      if (id == AwesomeJobUuid)
        IO.pure(1)
      else
        IO.pure(0)
  }

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  // test subject
  val jobRoutes: HttpRoutes[IO] = JobRoutes[IO](jobs).routes

  // tests
  "JobRoutes" - {
    "should return a job with a given id" in {
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.GET, uri = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
        )
        job <- response.as[Job]

      } yield {
        response.status shouldBe Status.Ok
        job shouldBe AwesomeJob
      }
    }

    "should return all jobs" in {
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.GET, uri = uri"/jobs")
        )
        jobs <- response.as[List[Job]]
      } yield {
        response.status shouldBe Status.Ok
        jobs shouldBe List(AwesomeJob)
      }
    }

    "should create a new job" in {
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/jobs")
            .withEntity(AwesomeJob.jobInfo)
        )
        jobId <- response.as[UUID]
      } yield {
        response.status shouldBe Status.Created
        jobId shouldBe NewJobUuid
      }
    }

    "should only update a job that exist" in {
      for {
        responseOk <- jobRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
            .withEntity(UpdatedAwesomeJob.jobInfo)
        )
        responseNotFound <- jobRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/jobs/843df718-ec6e-4d49-9289-000000000000")
            .withEntity(UpdatedAwesomeJob.jobInfo)
        )
      } yield {
        responseOk.status shouldBe Status.Ok
        responseNotFound.status shouldBe Status.NotFound
      }
    }

    "should only delete a job that exist" in {
      for {
        responseOk <- jobRoutes.orNotFound.run(
          Request(method = Method.DELETE, uri = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
        )
        responseNotFound <- jobRoutes.orNotFound.run(
          Request(method = Method.DELETE, uri = uri"/jobs/843df718-ec6e-4d49-9289-000000000000")
        )
      } yield {
        responseOk.status shouldBe Status.NoContent
        responseNotFound.status shouldBe Status.NotFound
      }
    }
  }

}
