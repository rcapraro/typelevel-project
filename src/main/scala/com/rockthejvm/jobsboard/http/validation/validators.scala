package com.rockthejvm.jobsboard.http.validation

import cats.data.{Validated, ValidatedNel}
import cats.implicits.*
import cats.syntax.validated.*
import com.rockthejvm.jobsboard.domain.job.*

import java.net.{URI, URL}
import scala.util.{Failure, Success, Try}

object validators {

  sealed trait ValidationFailure(val errorMessage: String)
  case class EmptyField(fieldName: String) extends ValidationFailure(s"'$fieldName' is empty")
  case class InvalidUrl(fieldName: String) extends ValidationFailure(s"'$fieldName' is not a valid URL")

  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  private def validateRequired[A](field: A, fieldName: String)(required: A => Boolean): ValidationResult[A] =
    if (required(field)) field.validNel
    else EmptyField(fieldName).invalidNel

  private def validateUrl(field: String, fieldName: String): ValidationResult[String] =
    Try(URI(field).toURL) match
      case Success(_) => field.validNel
      case Failure(_) => InvalidUrl(fieldName).invalidNel

  given jobInfoValidator: Validator[JobInfo] = (jobInfo: JobInfo) => {
    val JobInfo(
      company,
      title,
      description,
      externalUrl,
      remote,
      location,
      salaryLow,
      salaryHigh,
      currency,
      country,
      tags,
      image,
      seniority,
      other
    ) = jobInfo

    val validCompany     = validateRequired(company, "company")(_.nonEmpty)
    val validTitle       = validateRequired(title, "title")(_.nonEmpty)
    val validDescription = validateRequired(description, "description")(_.nonEmpty)
    val validExternalUrl = validateUrl(externalUrl, "externalUrl")
    val validLocation    = validateRequired(location, "location")(_.nonEmpty)

    (
      validCompany,
      validTitle,
      validDescription,
      validExternalUrl,
      remote.validNel,
      validLocation,
      salaryLow.validNel,
      salaryHigh.validNel,
      currency.validNel,
      country.validNel,
      tags.validNel,
      image.validNel,
      seniority.validNel,
      other.validNel
    ).mapN(JobInfo.apply)
  }

}
