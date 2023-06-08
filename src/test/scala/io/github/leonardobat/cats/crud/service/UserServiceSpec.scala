package io.github.leonardobat.cats.crud.service

import cats.data.EitherT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import cats.syntax.all.*
import io.github.leonardobat.cats.crud.model.User
import io.github.leonardobat.cats.crud.repository.UserRepository
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar.mock
import org.mockito.Mockito.{when, verify}


class UserServiceSpec extends AsyncWordSpec with Matchers with ScalaCheckDrivenPropertyChecks with MockitoSugar {

  import io.github.leonardobat.cats.crud.aux.Generators.*

  "UserService" should {
    "return all users" in {
      forAll { (users: Seq[User]) =>
        val userRepositoryMock = mock[UserRepository[IO]]
        when(userRepositoryMock.getAllUsers).thenReturn(EitherT.rightT[IO, Seq[User]](users))

        val userService = new UserService[IO](userRepositoryMock)
        val result = userService.getAllUsers.value

        result.unsafeRunSync() shouldBe Right(users)
      }
    }

    "return user by id" in {
      forAll { (user: User) =>
        val userRepositoryMock = mock[UserRepository[IO]]
        when(userRepositoryMock.getUserById(user.id)).thenReturn(EitherT.rightT[IO, User](user))

        val userService = UserService[IO](userRepositoryMock)
        val result = userService.getUserById(user.id).value

        result.unsafeRunSync() shouldBe Right(user)
      }
    }

    "create a user" in {
      forAll { (user: User) =>
        val userRepositoryMock = mock[UserRepository[IO]]
        when(userRepositoryMock.createUser(user)).thenReturn(EitherT.rightT[IO, Unit](()))

        val userService = new UserService[IO](userRepositoryMock)
        val result = userService.createUser(user).value

        verify(userRepositoryMock).createUser(user)
        result.unsafeRunSync() shouldBe Right(())
      }
    }

    "update a user" in {
      forAll { (user: User) =>
        val userRepositoryMock = mock[UserRepository[IO]]
        when(userRepositoryMock.updateUser(user)).thenReturn(EitherT.rightT[IO, Unit](()))

        val userService = new UserService[IO](userRepositoryMock)
        val result = userService.updateUser(user).value

        verify(userRepositoryMock).updateUser(user)
        result.unsafeRunSync() shouldBe Right (())
      }
    }

    "delete a user" in {
      forAll { (userId: Long) =>
        val userRepositoryMock = mock[UserRepository[IO]]
        when(userRepositoryMock.deleteUser(userId)).thenReturn(EitherT.rightT[IO, Unit](()))

        val userService = new UserService[IO](userRepositoryMock)
        val result = userService.deleteUser(userId).value

        verify(userRepositoryMock).deleteUser(userId)
        result.unsafeRunSync() shouldBe Right (())
      }
    }
  }
}
