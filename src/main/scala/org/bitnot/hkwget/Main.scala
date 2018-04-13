package org.bitnot.hkwget

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.bitnot.hkwget.models.Profile
import org.bitnot.hkwget.services.{DummyHackeRankAuth, HackerRankHttpService, LocalFileStore}


object Main extends App with LazyLogging {
  val config = ConfigFactory.load()
  val outputDir = config.getString("hkwget.outputDir")
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
  logger.debug(s"#submissions: ${maybeSubmissions}")

  for (submissions <- maybeSubmissions) {
    val localProfile = Profile.from(submissions)
    logger.info(s"saving profile")

    val store = new LocalFileStore(outputDir, false)
    store.save(localProfile)
  }


}
