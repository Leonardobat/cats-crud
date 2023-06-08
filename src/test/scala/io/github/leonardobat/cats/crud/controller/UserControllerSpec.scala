package io.github.leonardobat.cats.crud.controller

import cats.data.EitherT
import cats.effect.IO
import cats.effect.IO.asyncForIO
import cats.effect.unsafe.implicits.global
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import io.github.leonardobat.cats.crud.model.User
import io.github.leonardobat.cats.crud.service.UserService
import org.http4s.Method.*
import org.http4s.Status.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.{Request, Uri}
import org.mockito.Mockito.{verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class UserControllerSpec extends AsyncWordSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  import io.github.leonardobat.cats.crud.aux.Generators.*

  private val userServiceMock = mock[UserService[IO]]
  private val userController = new UserController[IO](userServiceMock)
  private val routes = Router("/" -> userController.routes).orNotFound

  "UserController" should {
    "return all users" in {
      forAll { (users: Seq[User]) =>
        when(userServiceMock.getAllUsers).thenReturn(EitherT.rightT[IO, Seq[User]](users))

        val request = Request[IO](GET, uri"/users")
        val response = routes.run(request).unsafeRunSync()

        response.status shouldBe Ok
        response.as[Seq[User]].unsafeRunSync() should contain theSameElementsAs users
      }
    }

    "return user by id" in {
      forAll { (user: User) =>
        val userId = user.id
        when(userServiceMock.getUserById(userId)).thenReturn(EitherT.rightT[IO, User](user))

        val request = Request[IO](GET, uri"users" / s"$userId")
        val response = routes.run(request).unsafeRunSync()

        response.status shouldBe Ok
        response.as[User].unsafeRunSync() shouldBe user
      }
    }

    "create a new user" in {
      forAll { (user: User) =>
        when(userServiceMock.createUser(user)).thenReturn(EitherT.rightT[IO, Unit](()))

        val request = Request[IO](POST, uri"/users").withEntity(user)
        val response = routes.run(request).unsafeRunSync()

        response.status shouldBe Created
      }
    }

    "update an existing user" in {
      forAll { (user: User) =>
        when(userServiceMock.updateUser(user)).thenReturn(EitherT.rightT[IO, Unit](()))

        val request = Request[IO](PUT, uri"/users").withEntity(user)
        val response = routes.run(request).unsafeRunSync()

        response.status shouldBe NoContent
      }
    }

    "delete a user" in {
      forAll { (userId: Long) =>
        when(userServiceMock.deleteUser(userId)).thenReturn(EitherT.rightT[IO, Unit](()))

        val request = Request[IO](DELETE, uri"users" / s"$userId")
        val response = routes.run(request).unsafeRunSync()

        response.status shouldBe NoContent
      }
    }
  }
}

