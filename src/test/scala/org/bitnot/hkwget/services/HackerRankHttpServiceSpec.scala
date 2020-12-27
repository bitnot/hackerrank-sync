package org.bitnot.hkwget.services

import org.bitnot.hkwget.JsonStubs._
import org.bitnot.hkwget.services.HackerRankAuth.NewRequest
import org.scalatest.OneInstancePerTest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client.testing.SttpBackendStub

import scala.language.postfixOps


class HackerRankHttpServiceSpec
  extends AnyFlatSpec
    with Matchers
    with OneInstancePerTest {
  behavior of "HackerRankHttpService"

  private val login: String = "hacker1"
  // https://github.com/softwaremill/sttp/blob/3d8b615c89c276ff1c2db349688079a6d9f5405c/docs/testing.rst
  private implicit val testingBackend: sttp.client.SttpBackend[sttp.client.Identity, Nothing, sttp.client.NothingT] =
    SttpBackendStub.synchronous
      // http://api.hackerrank.com/checker/languages.json
      .whenRequestMatches(
        _.uri.path.startsWith(Seq("checker", "languages.json")))
      .thenRespond(languages)
      // https://www.hackerrank.com/rest/contests/master/submissions/?offset=0&limit=1000
      .whenRequestMatches(
        _.uri.path.startsWith(Seq("rest", "contests", "master", "submissions")))
      .thenRespond(previews)
      // https://www.hackerrank.com/rest/contests/master/challenges/2d-array/submissions/123
      .whenRequestMatches(
        _.uri.path.startsWith(Seq("rest", "contests", "master", "challenges")))
      .thenRespond(singleSubmission)
      // https://www.hackerrank.com/rest/hackers/${login}/contest_participation?offset=${offset}&limit=${limit}
      .whenRequestMatches(_.uri.path.startsWith(Seq("rest", "hackers", login)))
      .thenRespond(contestParticipations)

  private implicit val auth: HackerRankAuth = (req: NewRequest) => req
  private val service = new HackerRankHttpService(login)

  "getContestParticipations" should "return 11 contests from JSON mock" in {
    val resp = service.getContestParticipations()
    resp.isSuccess shouldEqual true
    resp.get.models.size shouldEqual 11
  }

  "getLanguages" should "return 63 languages from JSON mock" in {
    val resp = service.getLanguages()
    resp.isSuccess shouldEqual true
    resp.get.languages.codes.size shouldEqual 63
  }

  "getSubmissions" should "return 7 accepted answers from JSON mock" in {
    val resp = service.getSubmissions()
    resp.isSuccess shouldEqual true
    resp.get.size shouldEqual 7
    resp.get.head.id shouldEqual 14419845
  }
}
