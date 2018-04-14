package org.bitnot.hkwget.models.hackerrank

import io.circe.generic.auto._
import io.circe.java8.time._
import io.circe.parser._
import org.bitnot.hkwget.JsonStubs
import org.bitnot.hkwget.models.Profile
import org.bitnot.hkwget.models.hackerrank.{ApiResponse, SubmissionPreview, SubmissionResponse}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers, OneInstancePerTest}

import scala.language.postfixOps

class CirceDecodeSpec
  extends FlatSpec
    with Matchers
    with OneInstancePerTest
    with MockFactory {
  behavior of "Circe"

  private def checkError[Status <: Either[io.circe.Error, _]](status:Status)={
    if(status.isLeft){
      println(s"Error decoding: ${status.left.get}")
    }
  }

  it should "decode single submission" in {
    lazy val single = decode[SubmissionResponse](JsonStubs.singleSubmission)

    checkError(single)
    single.isRight shouldEqual true
  }

  it should "decode list of submission previews" in {
    lazy val previews = decode[ApiResponse[SubmissionPreview]](JsonStubs.previews)

    checkError(previews)
    previews.isRight shouldEqual true
  }

  it should "decode list of languages" in {
    lazy val languages = decode[LanguagesResponse](JsonStubs.languages)

    checkError(languages)
    languages.isRight shouldEqual true
  }

  it should "decode list of contest participation" in {
    import org.bitnot.hkwget.helpers.CustomDecoders._
    lazy val participations = decode[ApiResponse[ContestParticipation]](JsonStubs.contestParticipations)

    checkError(participations)
    participations.isRight shouldEqual true
  }

}
