package io.github.leonardobat.cats.crud.repository

import cats.data.EitherT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import cats.syntax.all.*
import io.github.leonardobat.cats.crud.model.User
import io.github.leonardobat.cats.crud.repository.InMemoryUserRepository
import org.scalacheck.Arbitrary
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class InMemoryUserRepositorySpec extends AsyncWordSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  import io.github.leonardobat.cats.crud.aux.Generators.*

  "InMemoryUserRepository" should {
    "return all users" in {
      forAll { (users: Seq[User]) =>
        val repository = InMemoryUserRepository[IO]
        val result = for {
          _ <- users.traverse(repository.createUser)
          fetchedUsers <- repository.getAllUsers
        } yield fetchedUsers

        result.value.unsafeRunSync() shouldBe Right(users)
      }
    }

    "return user by id" in {
      forAll { (user: User) =>
        val repository = InMemoryUserRepository[IO]
        val result = for {
          _ <- repository.createUser(user)
          fetchedUser <- repository.getUserById(user.id)
        } yield fetchedUser

        result.value.unsafeRunSync() shouldBe Right(user)
      }
    }

    "return None for non-existent user by id" in {
      forAll { (userId: Long) =>
        val repository = InMemoryUserRepository[IO]
        val result = repository.getUserById(userId)
        result.value.unsafeRunSync() shouldBe Left("User not found")
      }
    }

    "create a user" in {
      forAll { (user: User) =>
        val repository = InMemoryUserRepository[IO]
        val result = for {
          _ <- repository.createUser(user)
          fetchedUser <- repository.getUserById(user.id)
        } yield fetchedUser

        result.value.unsafeRunSync() shouldBe Right(user)
      }
    }

    "update a user" in {
      forAll { (user: User) =>
        val repository = InMemoryUserRepository[IO]
        val updatedUser = user.copy(name = "Updated User")
        val result = for {
          _ <- repository.createUser(user)
          _ <- repository.updateUser(updatedUser)
          fetchedUser <- repository.getUserById(user.id)
        } yield fetchedUser

        result.value.unsafeRunSync() shouldBe Right(updatedUser)
      }
    }

    "delete a user" in {
      forAll { (user: User) =>
        val repository = InMemoryUserRepository[IO]
        val result = for {
          _ <- repository.createUser(user)
          _ <- repository.deleteUser(user.id)
          fetchedUser <- repository.getUserById(user.id)
        } yield fetchedUser
        result.value.unsafeRunSync() shouldBe Left("User not found")
      }
    }
  }
}
