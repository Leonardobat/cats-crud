package io.github.leonardobat.cats.crud.service

import cats.data.EitherT
import cats.effect.Async
import io.github.leonardobat.cats.crud.model.User
import io.github.leonardobat.cats.crud.repository.UserRepository

class UserService[F[_] : Async](private val userRepository: UserRepository[F]) {

  def getAllUsers: EitherT[F, String, Seq[User]] =
    userRepository.getAllUsers

  def getUserById(userId: Long): EitherT[F, String, User] =
    userRepository.getUserById(userId)

  def createUser(user: User): EitherT[F, String, Unit] =
    userRepository.createUser(user)

  def updateUser(user: User): EitherT[F, String, Unit] =
    userRepository.updateUser(user)

  def deleteUser(userId: Long): EitherT[F, String, Unit] =
    userRepository.deleteUser(userId)
}