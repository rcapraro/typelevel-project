package com.rockthejvm.jobsboard.config

import cats.{MonadError, MonadThrow}
import pureconfig.{ConfigReader, ConfigSource}
import cats.syntax.flatMap.*
import pureconfig.error.ConfigReaderException

import scala.reflect.ClassTag

object Syntax {
  extension (source: ConfigSource)
    def loadF[F[_], A](using reader: ConfigReader[A], F: MonadThrow[F], tag: ClassTag[A]): F[A] =
      F.pure(source.load[A]).flatMap {
        case Left(errors) => F.raiseError[A](ConfigReaderException(errors))
        case Right(value) => F.pure(value)
      }

}
