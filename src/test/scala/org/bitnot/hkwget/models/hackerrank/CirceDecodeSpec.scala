package org.bitnot.hkwget.models.hackerrank

import io.circe.generic.auto._
import io.circe.parser._
import org.bitnot.hkwget.JsonStubs
import org.scalatest.{EitherValues, OneInstancePerTest}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class CirceDecodeSpec
  extends AnyFlatSpec
    with Matchers
    with OneInstancePerTest
    with EitherValues {
  behavior of "Circe"

  val right = Symbol("right")

  it should "decode single submission" in {
    lazy val single = decode[SubmissionResponse](JsonStubs.singleSubmission)

    single should be(right)
  }

  it should "decode list of submission previews" in {
    lazy val previews = decode[ApiResponse[SubmissionPreview]](JsonStubs.previews)

    previews should be (right)
  }

  it should "decode list of languages" in {
    lazy val languages = decode[LanguagesResponse](JsonStubs.languages)

    languages should be(right)
  }

  it should "decode list of contest participation" in {
    import org.bitnot.hkwget.helpers.CustomDecoders._
    lazy val participations = decode[ApiResponse[ContestParticipation]](JsonStubs.contestParticipations)

    participations should be(right)
  }

}
