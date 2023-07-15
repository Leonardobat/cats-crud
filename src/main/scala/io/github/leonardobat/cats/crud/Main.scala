package io.github.leonardobat.cats.crud

import cats.effect.IO.asyncForIO
import cats.effect.{Async, ExitCode, IO, IOApp}
import com.comcast.ip4s.{Host, Port, port}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import io.circe.config.parser
import io.circe.config.syntax.*
import io.circe.generic.auto.*
import io.github.leonardobat.cats.crud.controller.UserController
import io.github.leonardobat.cats.crud.repository.InMemoryUserRepository
import io.github.leonardobat.cats.crud.service.UserService
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.Request

object Main extends IOApp with LazyLogging {

  def run(args: List[String]): IO[ExitCode] = {
    val server = for {
      _ <- IO(logger.info("Starting web server"))
      appRoutes <- createApp[IO]
      config <- parser.decodePathF[IO, ServerSettings]("app.server")
      exitCode <- EmberServerBuilder
        .default[IO]
        .withHttpApp(appRoutes)
        .withHostOption(Host.fromString(config.host))
        .withPort(Port.fromInt(config.port).getOrElse(port"9001"))
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield exitCode

    server
  }

  private def createApp[F[_] : Async]: F[HttpApp[F]] = {
    val userRepository = new InMemoryUserRepository[F]
    val userService = new UserService[F](userRepository)
    val userController = new UserController[F](userService)

    val endpoints = userController.routes
    val appRoutes = Router[F](
      "/" -> endpoints,
    ).orNotFound

    Async[F].pure(appRoutes)
  }

  private case class ServerSettings(host: String, port: Int)
}