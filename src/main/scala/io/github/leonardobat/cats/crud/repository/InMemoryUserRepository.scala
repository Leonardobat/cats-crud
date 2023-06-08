package io.github.leonardobat.cats.crud.repository

import cats.data.EitherT
import cats.effect.{Async, Ref}
import cats.implicits.*
import io.github.leonardobat.cats.crud.model.User

class InMemoryUserRepository[F[_] : Async] extends UserRepository[F] {
  private val usersRef: Ref[F, Seq[User]] = Ref.unsafe(Seq.empty)

  def getAllUsers: EitherT[F, String, Seq[User]] = {
    EitherT.right(usersRef.get)
  }

  def getUserById(id: Long): EitherT[F, String, User] = {
    EitherT.fromOptionF(usersRef.get.map(_.find(_.id == id)), "User not found")
  }

  def createUser(user: User): EitherT[F, String, Unit] = {
    EitherT.liftF(usersRef.update(users => users :+ user)).map(_ => ())
  }

  def updateUser(user: User): EitherT[F, String, Unit] = {
    EitherT.fromOptionF(usersRef.get.map(_.find(_.id == user.id)), "User not found")
      .flatMap(_ => EitherT.liftF(usersRef.update(users => users.map(u => if (u.id == user.id) user else u))).map(_ => ()))
  }

  def deleteUser(id: Long): EitherT[F, String, Unit] = {
    EitherT.liftF(usersRef.update(users => users.filterNot(_.id == id))).map(_ => ())
  }
}
