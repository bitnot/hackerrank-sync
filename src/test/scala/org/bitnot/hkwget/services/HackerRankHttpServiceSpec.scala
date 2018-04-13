package org.bitnot.hkwget.services

import com.softwaremill.sttp.Id
import com.softwaremill.sttp.testing.SttpBackendStub
import org.bitnot.hkwget.services.HackeRankAuth.NewRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers, OneInstancePerTest}

import scala.language.postfixOps

class HackerRankHttpServiceSpec
  extends FlatSpec
    with Matchers
    with OneInstancePerTest
    with MockFactory {
  behavior of "HackerRankHttpService"

  lazy val contests =
    scala.io.Source.fromResource("contests.json").getLines().mkString
  lazy val previews =
    scala.io.Source.fromResource("submissions.json").getLines().mkString
  lazy val singleSubmission =
    scala.io.Source.fromResource("submission.json").getLines().mkString
  lazy val languages =
    scala.io.Source.fromResource("languages.json").getLines().mkString

  // https://github.com/softwaremill/sttp/blob/3d8b615c89c276ff1c2db349688079a6d9f5405c/docs/testing.rst
  implicit val testingBackend: SttpBackendStub[Id, Nothing] =
    SttpBackendStub.synchronous
      // http://api.hackerrank.com/checker/languages.json
      .whenRequestMatches(
        _.uri.path.startsWith(List("checker", "languages.json")))
      .thenRespond(languages)
      // https://www.hackerrank.com/rest/contests/upcoming?offset=0&limit=10&contest_slug=active
      .whenRequestMatches(
      _.uri.path.startsWith(List("rest", "contests", "upcoming")))
      .thenRespond(contests)
      // https://www.hackerrank.com/rest/contests/master/submissions/?offset=0&limit=1000
      .whenRequestMatches(
      _.uri.path.startsWith(List("rest", "contests", "master", "submissions")))
      .thenRespond(previews)
      // https://www.hackerrank.com/rest/contests/master/challenges/2d-array/submissions/14419845
      .whenRequestMatches(
      _.uri.path.startsWith(List("rest", "contests", "master", "challenges")))
      .thenRespond(singleSubmission)


  implicit val auth = new HackeRankAuth {
    override def setHeaders(req: NewRequest): NewRequest = req
  }
  val service = new HackerRankHttpService(auth)

  "getLanguages" should "return 63 entities" in {
    val resp = service.getLanguages()
    resp.isSuccess shouldEqual true
    resp.get.languages.codes.size shouldEqual 63
  }

  "getContests" should "return 15 entities" in {
    val resp = service.getContests()
    resp.isSuccess shouldEqual true
    resp.get.total shouldEqual 15
    resp.get.models.size shouldEqual 15
    resp.get.page shouldEqual Some(1)
  }

  "getSubmissions" should "return 1 entity" in {
    val resp = service.getSubmissions()
    resp.isSuccess shouldEqual true
    resp.get.size shouldEqual 7 //there is 7 "Accepted" in submissions.json
    resp.get.head.id shouldEqual 14419845
  }
}
