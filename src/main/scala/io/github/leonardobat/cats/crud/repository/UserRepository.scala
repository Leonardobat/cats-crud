package io.github.leonardobat.cats.crud.repository

import cats.data.EitherT
import io.github.leonardobat.cats.crud.model.User

trait UserRepository[F[_]] {

  def getAllUsers: EitherT[F, String, Seq[User]]

  def getUserById(id: Long): EitherT[F, String, User]

  def createUser(user: User): EitherT[F, String, Unit]

  def updateUser(user: User): EitherT[F, String, Unit]

  def deleteUser(id: Long): EitherT[F, String, Unit]
}