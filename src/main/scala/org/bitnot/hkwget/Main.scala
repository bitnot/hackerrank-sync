package org.bitnot.hkwget

import com.softwaremill.sttp.HttpURLConnectionBackend
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.bitnot.hkwget.models.Profile
import org.bitnot.hkwget.services.{DummyHackeRankAuth, HackeRankAuth, HackerRankHttpService, LocalFileStore}

import scala.concurrent.duration.Duration


object Main extends App with LazyLogging {
  val config = HKConfig(ConfigFactory.load().getConfig("hkwget"))
  val timeToLookBackO = if (config.timeToLookBack == Duration.Zero) None else Some(config.timeToLookBack)
  val maxPerContestO = if (config.maxPerContest == 0) None else Some(config.maxPerContest)
  val store = new LocalFileStore(config.outputDir, false)
  val cookieTxt = scala.io.Source.fromResource("cookies.txt").getLines().mkString
  implicit val auth: HackeRankAuth = DummyHackeRankAuth(cookieTxt)
  implicit val backend = HttpURLConnectionBackend()
  val hkService = new HackerRankHttpService(config.login, config.contestBlackList.toSet)
  hkService.getSubmissions(timeToLookBackO, maxPerContestO)
    .map(Profile(_))
    .foreach { localProfile =>
      logger.info(s"saving profile")
      store.save(localProfile)
    }
}
