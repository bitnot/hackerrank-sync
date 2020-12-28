package org.bitnot.hkwget

import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}
import com.typesafe.config.ConfigFactory
import org.bitnot.hkwget.helpers.LazyLogging
import io.github.resilience4j.ratelimiter.{RateLimiter, RateLimiterConfig}
import org.bitnot.hkwget.helpers.RateLimitingSttpBackend
import org.bitnot.hkwget.models.Profile
import org.bitnot.hkwget.services.{HackerRankAuth, HackerRankHttpService, LocalFileStore, SessionCookieAuth}
import sttp.client.monad.IdMonad

import java.time.Duration


object Main extends App with LazyLogging {
  val config = HKConfig(ConfigFactory.load().getConfig("hkwget"))
  val timeToLookBack = Option(config.timeToLookBack).filter(_ != Duration.ZERO)
  val maxPerContest = Option(config.maxPerContest).filter(_ != 0)
  val store = new LocalFileStore(config.outputDir, false)

  implicit val auth: HackerRankAuth = SessionCookieAuth(config.sessionCookieValue)
  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = new RateLimitingSttpBackend[Identity, Nothing, NothingT](
    RateLimiter.of("take it easy",
      RateLimiterConfig.custom()
        .limitForPeriod(1)
        .limitRefreshPeriod(Duration.ofSeconds(6))
        .timeoutDuration(Duration.ofSeconds(30))
        .build()
    ),
    HttpURLConnectionBackend()
  )(IdMonad)

  val hkService = new HackerRankHttpService(config.login, config.contestBlackList.toSet)

  hkService.getSubmissions(timeToLookBack, maxPerContest)
    .map(Profile(_))
    .foreach { localProfile =>
      logger.info(s"saving profile")
      store.save(localProfile)
    }
}
