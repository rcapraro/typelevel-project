package com.rockthejvm.jobsboard.domain

import java.util.UUID

object job {

  case class Job(
      id: UUID,
      date: Long,
      ownerEmail: String,
      jobInfo: JobInfo,
      active: Boolean = false
  )

  case class JobInfo(
      company: String,
      title: String,
      description: String,
      externalUrl: String,
      remote: Boolean,
      location: String,
      salaryLow: Option[Int],
      salaryHigh: Option[Int],
      currency: Option[String],
      country: Option[String],
      tags: Option[List[String]],
      image: Option[String],
      seniority: Option[String],
      other: Option[String]
  )

  object JobInfo {
    val empty: JobInfo = JobInfo("", "", "", "", false, "", None, None, None, None, None, None, None, None)
    def minimal(company: String, title: String, description: String, externalUrl: String, remote: Boolean, location: String): JobInfo =
      JobInfo(company: String, title, description, externalUrl, remote, location, None, None, None, None, None, None, None, None)
  }

  final case class JobFilter(
      companies: List[String] = List.empty,
      locations: List[String] = List.empty,
      countries: List[String] = List.empty,
      seniorities: List[String] = List.empty,
      tags: List[String] = List.empty,
      maxSalary: Option[Int] = None,
      remote: Boolean = false
  )

}
