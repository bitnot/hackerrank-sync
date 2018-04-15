package org.bitnot.hkwget

import com.softwaremill.sttp.HttpURLConnectionBackend
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.bitnot.hkwget.models.Profile
import org.bitnot.hkwget.services.{DummyHackeRankAuth, HackeRankAuth, HackerRankHttpService, LocalFileStore}

import scala.collection.JavaConverters._

object Main extends App with LazyLogging {
  val config = ConfigFactory.load()
  val contestBlackList = config.getStringList("hkwget.contestBlackList").asScala
  val outputDir = config.getString("hkwget.outputDir")
  val maxSubmissionsPerContestToSave = config.getInt("hkwget.maxSubmissionsPerContestToSave")
  val timeToLookBack = config.getDuration("hkwget.timeToLookBack")
  val login = config.getString("hkwget.auth.login")
  // val password = config.getString("hkwget.auth.password")
  val cookieTxt =
    scala.io.Source.fromResource("cookies.txt").getLines().mkString

  implicit val auth: HackeRankAuth = DummyHackeRankAuth(cookieTxt, login)
  implicit val backend = HttpURLConnectionBackend()
  val hkService = new HackerRankHttpService(contestBlackList.toSet)

  val maybeSubmissions = hkService.getSubmissions(maxSubmissionsPerContestToSave, timeToLookBack)
  for (submissions <- maybeSubmissions) {
    val localProfile = Profile.from(submissions)
    logger.info(s"saving profile")

    val store = new LocalFileStore(outputDir, false)
    store.save(localProfile)
  }

}
