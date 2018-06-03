package org.bitnot.hkwget

import com.softwaremill.sttp.HttpURLConnectionBackend
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.bitnot.hkwget.models.Profile
import org.bitnot.hkwget.services.{
  DummyHackeRankAuth,
  HackeRankAuth,
  HackerRankHttpService,
  LocalFileStore
}

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

object Main extends App with LazyLogging {
  val config = ConfigFactory.load()
  val contestBlackList = config.getStringList("hkwget.contestBlackList").asScala
  val outputDir = config.getString("hkwget.outputDir")
  val maxPerContest = config.getInt("hkwget.maxSubmissionsPerContestToSave")
  val timeToLookBack = config.getDuration("hkwget.timeToLookBack")
  val login = config.getString("hkwget.auth.login")
  // val password = config.getString("hkwget.auth.password")
  val cookieTxt =
    scala.io.Source.fromResource("cookies.txt").getLines().mkString

  val store = new LocalFileStore(outputDir, false)

  implicit val auth: HackeRankAuth = DummyHackeRankAuth(cookieTxt)
  implicit val backend = HttpURLConnectionBackend()
  val hkService = new HackerRankHttpService(login, contestBlackList.toSet)

  val maybeSubmissions = hkService.getSubmissions(
    if (timeToLookBack == Duration.Zero) None else Some(timeToLookBack),
    if (maxPerContest == 0) None else Some(maxPerContest))

  for (submissions <- maybeSubmissions) {
    val localProfile = Profile.apply(submissions)
    logger.info(s"saving profile")
    store.save(localProfile)
  }

}
