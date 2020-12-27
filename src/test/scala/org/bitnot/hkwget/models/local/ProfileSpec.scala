package org.bitnot.hkwget.models.local

import io.circe.generic.auto._
import io.circe.parser._
import org.bitnot.hkwget.JsonStubs
import org.bitnot.hkwget.models.Profile
import org.bitnot.hkwget.models.hackerrank.{ApiResponse, SubmissionPreview, SubmissionResponse}
import org.scalatest.OneInstancePerTest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps


class ProfileSpec
  extends AnyFlatSpec
    with Matchers
    with OneInstancePerTest {
  behavior of "Profile"

  lazy val single = decode[SubmissionResponse](JsonStubs.singleSubmission)
  lazy val onlineSubmission = single.right.get.model

  lazy val previews = decode[ApiResponse[SubmissionPreview]](JsonStubs.previews)
  lazy val submissions = previews.right.get.models.map { case p =>
    onlineSubmission.copy(
      id = p.id,
      challenge_id = p.challenge_id,
      slug = p.challenge.slug,
      challenge_slug = p.challenge.slug,
      contest_id = p.contest_id,
      status = p.status,
      language = p.language
    )
  }.toSeq

  println(previews)
  println(submissions)

  "apply" should "aggregated accepted by challenge and language" in {
    val profile = Profile.apply(submissions)

    profile.contests.size shouldEqual 1
    profile.contests.head.slug shouldEqual "master"
    profile.contests.head.challenges.size shouldEqual 7
  }

}
