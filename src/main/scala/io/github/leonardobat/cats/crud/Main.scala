package io.github.leonardobat.cats.crud

import cats.effect.IO.asyncForIO
import cats.effect.{Async, ExitCode, IO, IOApp}
import com.comcast.ip4s.Port
import io.github.leonardobat.cats.crud.controller.UserController
import io.github.leonardobat.cats.crud.repository.InMemoryUserRepository
import io.github.leonardobat.cats.crud.service.UserService
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.Router
import sttp.tapir.docs.openapi.*
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val server = for {
      appRoutes <- createApp[IO]
      exitCode <- EmberServerBuilder
        .default[IO]
        .withHttpApp(appRoutes)
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
}