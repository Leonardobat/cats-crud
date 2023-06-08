package io.github.leonardobat.cats.crud.controller

import cats.effect.Async
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.github.leonardobat.cats.crud.model.User
import io.github.leonardobat.cats.crud.service.UserService
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

class UserController[F[_] : Async](private val userService: UserService[F]) {
  private val getAllUsers =
    endpoint.get
      .in("users")
      .out(jsonBody[Seq[User]])
      .errorOut(stringBody)
      .serverLogic(_ => userService.getAllUsers.value)

  private val getUserById =
    endpoint.get
      .in("users" / path[Long])
      .out(jsonBody[User])
      .errorOut(stringBody)
      .serverLogic(userId => userService.getUserById(userId).value)

  private val createUser =
    endpoint.post
      .in("users")
      .in(jsonBody[User])
      .out(statusCode(StatusCode(201)))
      .errorOut(stringBody)
      .serverLogic(user => userService.createUser(user).value)

  private val updateUser =
    endpoint.put
      .in("users")
      .in(jsonBody[User])
      .out(statusCode(StatusCode(204)))
      .errorOut(stringBody)
      .serverLogic(user => userService.updateUser(user).value)

  private val deleteUser =
    endpoint.delete
      .in("users" / path[Long])
      .out(statusCode(StatusCode(204)))
      .errorOut(stringBody)
      .serverLogic(userId => userService.deleteUser(userId).value)

  private val serverEndpoints: List[ServerEndpoint[Any, F]] = List(
    getAllUsers,
    getUserById,
    createUser,
    updateUser,
    deleteUser,
  )

  private val swaggerEndpoints = SwaggerInterpreter()
    .fromServerEndpoints[F](serverEndpoints, "Crud-Cats", "0.1")

  def routes: HttpRoutes[F] = Http4sServerInterpreter[F]().toRoutes(serverEndpoints ++ swaggerEndpoints)
}