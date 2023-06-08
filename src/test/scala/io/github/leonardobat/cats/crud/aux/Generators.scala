package io.github.leonardobat.cats.crud.aux

import io.github.leonardobat.cats.crud.model.User
import org.scalacheck.Arbitrary

object Generators {

  implicit val arbitraryUser: Arbitrary[User] = Arbitrary {
    for {
      id <- Arbitrary.arbitrary[Long]
      name <- Arbitrary.arbitrary[String]
      age <- Arbitrary.arbitrary[Int]
    } yield User(id, name, age)
  }
}
