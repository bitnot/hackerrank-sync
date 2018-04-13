package org.bitnot.hkwget

import org.bitnot.hkwget.models.Profile
import org.bitnot.hkwget.services.{DummyHackeRankAuth, HackerRankHttpService}

object Main extends App {

  import io.circe.java8.time._


  //  val authConf = ConfigFactory.load().getConfig("hkwget.auth")
  //  val login = authConf.getString("login")
  //  val password = authConf.getString("password")


  val cookieTxt =
    scala.io.Source.fromResource("cookies.txt").getLines().mkString

  val hkService = new HackerRankHttpService(DummyHackeRankAuth(cookieTxt))

  val maybeSubmissions = hkService.getSubmissions
  //  import io.circe.parser.decode
  //  val json = scala.io.Source.fromResource("submissions.json").getLines.mkString
  //  val submissions = decode[ApiResponse[SubmissionPreview]](json)
  println(s"#submissions: ${maybeSubmissions}")

  for (submissions <- maybeSubmissions) {
    val localProfile = Profile.from(submissions)
    println(s"#localProfile: ${localProfile}")
  }

}
