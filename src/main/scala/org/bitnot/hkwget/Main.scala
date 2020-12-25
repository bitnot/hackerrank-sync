package org.bitnot.hkwget

import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import io.github.resilience4j.ratelimiter.{RateLimiter, RateLimiterConfig}
import org.bitnot.hkwget.helpers.RateLimitingSttpBackend
import org.bitnot.hkwget.models.Profile
import org.bitnot.hkwget.services.{HackerRankAuth, HackerRankHttpService, LocalFileStore, SessionCookieAuth}
import sttp.client.monad.IdMonad

import java.time.Duration


object Main extends App with LazyLogging {
  val config = HKConfig(ConfigFactory.load().getConfig("hkwget"))
  val timeToLookBackO = if (config.timeToLookBack == Duration.ZERO) None else Some(config.timeToLookBack)
  val maxPerContestO = if (config.maxPerContest == 0) None else Some(config.maxPerContest)
  val store = new LocalFileStore(config.outputDir, false)
  implicit val auth: HackerRankAuth = SessionCookieAuth(config.sessionCookieValue)

  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = new RateLimitingSttpBackend[Identity, Nothing, NothingT](
    RateLimiter.of("default", RateLimiterConfig.ofDefaults),
    HttpURLConnectionBackend()
  )(IdMonad)
  val hkService = new HackerRankHttpService(config.login, config.contestBlackList.toSet)
  hkService.getSubmissions(timeToLookBackO, maxPerContestO)
    .map(Profile(_))
    .foreach { localProfile =>
      logger.info(s"saving profile")
      store.save(localProfile)
    }
}
