package org.bitnot.hkwget

import com.softwaremill.sttp.HttpURLConnectionBackend
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.bitnot.hkwget.models.Profile
import org.bitnot.hkwget.services.{HackeRankAuth, HackerRankHttpService, LocalFileStore, SessionCookieAuth}

import java.time.Duration


object Main extends App with LazyLogging {
  val config = HKConfig(ConfigFactory.load().getConfig("hkwget"))
  val timeToLookBackO = if (config.timeToLookBack == Duration.ZERO) None else Some(config.timeToLookBack)
  val maxPerContestO = if (config.maxPerContest == 0) None else Some(config.maxPerContest)
  val store = new LocalFileStore(config.outputDir, false)
  implicit val auth: HackeRankAuth = SessionCookieAuth(config.sessionCookieValue)
  implicit val backend = HttpURLConnectionBackend()
  val hkService = new HackerRankHttpService(config.login, config.contestBlackList.toSet)
  hkService.getSubmissions(timeToLookBackO, maxPerContestO)
    .map(Profile(_))
    .foreach { localProfile =>
      logger.info(s"saving profile")
      store.save(localProfile)
    }
}
