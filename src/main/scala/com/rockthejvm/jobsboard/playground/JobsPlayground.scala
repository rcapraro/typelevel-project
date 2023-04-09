package com.rockthejvm.jobsboard.playground

import cats.effect.*
import cats.implicits.*
import com.rockthejvm.jobsboard.core.LiveJobs
import com.rockthejvm.jobsboard.domain.job.JobInfo
import doobie.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.util.*

object JobsPlayground extends IOApp.Simple {

  private val postgresResource: Resource[IO, HikariTransactor[IO]] = for {
    ec <- ExecutionContexts.fixedThreadPool(32)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql:board",
      "docker",
      "docker",
      ec
    )
  } yield xa

  val jobInfo: JobInfo = JobInfo.minimal(
    company = "rock The JVM",
    title = "Software Developer",
    description = "Best job ever",
    externalUrl = "rockthejvm.com",
    remote = true,
    location = "anywhere"
  )

  override def run: IO[Unit] = postgresResource.use { xa =>
    for {
      jobs       <- LiveJobs[IO](xa)
      _          <- IO.println("Ready. Next...") *> IO.readLine
      id         <- jobs.create("daniel@rockthejvm.com", jobInfo)
      _          <- IO.println("Next...") *> IO.readLine
      list       <- jobs.all()
      _          <- IO.println(s"All jobs: $list. Next...") *> IO.readLine
      _          <- jobs.update(id, jobInfo.copy(title = "Software rockstar"))
      updatedJob <- jobs.find(id)
      _          <- IO.println(s"Updated job: $updatedJob. Next...") *> IO.readLine
      _          <- jobs.delete(id)
      listAfter  <- jobs.all()
      _          <- IO.println(s"Deleted job. List now: $listAfter. Next...") *> IO.readLine
    } yield ()
  }

}
