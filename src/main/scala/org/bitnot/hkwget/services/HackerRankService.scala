package org.bitnot.hkwget.services


import java.time.Duration

import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.circe.java8.time._
import org.bitnot.hkwget.helpers.CustomDecoders._
import org.bitnot.hkwget.helpers._
import org.bitnot.hkwget.models.hackerrank._
import org.bitnot.hkwget.services.HackeRankAuth.NewRequest

import scala.util._


trait HackerRankService {

  def getLanguages(): Try[LanguagesResponse]

  def getContestParticipations: Try[ApiResponse[ContestParticipation]]

  def getContests: Try[ApiResponse[Contest]]

  def getSubmissions(maxSubmissionsPerContestToSave: Int,
                     timeToLookBack: Duration): Try[Seq[Submission]]

  // todo: maybe ApiResponse[Submission] ?
}


trait HackeRankAuth {
  def login: String

  def setHeaders(req: NewRequest): NewRequest
}

object HackeRankAuth {
  type NewRequest = RequestT[Empty, String, Nothing]

  implicit class Authorizer(req: NewRequest)(implicit auth: HackeRankAuth) {
    def authorize(): NewRequest = auth.setHeaders(req)
  }

}

case class BasicHackeRankAuth(login: String, password: String)(
  implicit backend: SttpBackend[Id, Nothing]
) extends HackeRankAuth with LazyLogging {

  def setHeaders(req: NewRequest): NewRequest = {

    logger.debug(s"Authenticating $login")
    val loginResponse =
      sttp
        .post(Urls.login)
        .body(Credentials(login, password))
        .send()
    val CookieHeader = "Cookie"

    val cookies =
      loginResponse.cookies.map(p => p.name + "=" + p.value).mkString("; ")
    // TODO: FIX ME
    req.header(CookieHeader, cookies)
  }
}

case class DummyHackeRankAuth(cookies: String, login: String) extends HackeRankAuth with LazyLogging {
  def setHeaders(req: NewRequest): NewRequest = {
    val CookieHeader = "Cookie"
    logger.debug(s"Authenticating with cookie")
    req.header(CookieHeader, cookies)
  }
}


class HackerRankHttpService(contestBlackList: Set[String] = Set.empty)
                           (implicit
                            auth: HackeRankAuth,
                            backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
                           )
  extends HackerRankService with LazyLogging {

  import HackerRankHttpService.get


  def getLanguages(): Try[LanguagesResponse] =
    get[LanguagesResponse](Urls.languages)


  override def getSubmissions(maxSubmissionsPerContestToSave: Int = 0,
                              timeToLookBack: Duration = Duration.ZERO): Try[Seq[Submission]] = {
    logger.info("running getSubmissions")
    val sinceEver = timeToLookBack.isZero
    val sinceUnixSeconds =
      java.time.Instant.now.minus(timeToLookBack).toEpochMilli / 1000
    val takeAll = maxSubmissionsPerContestToSave == 0

    for {participations <- getContestParticipations} yield {
      val contestsToCheck: Seq[String] = "master" :: participations
        .models
        .filter(_.hacker_rank.isDefined)
        .map(_.slug)
        .toList

      contestsToCheck
        .filter(c => !contestBlackList.contains(c))
        .map {
          case contestName: String =>
            getSubmissionPreviews(contestName) match {
              case Success(previews) if previews.models.nonEmpty => {
                logger.debug(s"${previews.total} previews  in ${contestName}")
                val acceptedPreviews = previews.models.filter(_.accepted)
                val latestByChallengeByLang = takeLatestByChallengeByLang(acceptedPreviews)
                val filteredByDate =
                  if (sinceEver) latestByChallengeByLang
                  else latestByChallengeByLang.filter(_.created_at >= sinceUnixSeconds)
                val limited =
                  if (takeAll) filteredByDate
                  else filteredByDate.take(maxSubmissionsPerContestToSave) //TODO : maxSubmissionsPerContestToSave total, not per contest
                val submissions = limited
                  .map { preview =>
                    val maybeSubmission = getSubmission(contestName, preview)
                    maybeSubmission
                      .map(_.model)
                      .toOption
                  }
                  .flatten
                  .toSeq
                submissions
              }
              case _ => Seq.empty[Submission]
            }
        }.flatten
    }
  }


  private def getSubmission(contestName: String, preview: SubmissionPreview) = {
    get[SubmissionResponse](
      Urls.submission(
        preview.id,
        preview.challenge.slug,
        contest = contestName
      ))
  }

  def getContestParticipations: Try[ApiResponse[ContestParticipation]] =
    get[ApiResponse[ContestParticipation]](Urls.contestParticipation(auth.login))


  def getContests(): Try[ApiResponse[Contest]] =
    get[ApiResponse[Contest]](Urls.contests())


  private def getSubmissionPreviews(contestName: String = "master"): Try[ApiResponse[SubmissionPreview]] =
    get[ApiResponse[SubmissionPreview]](Urls.submissions(contest = contestName, limit = 1000))


  private def takeLatestByChallengeByLang(submissions: Seq[SubmissionPreview]) = {
    submissions
      .groupBy(s => (s.challenge, s.language))
      .map { case ((_, _), submissions) =>
        submissions.maxBy(_.created_at)
      }
  }
}


object HackerRankHttpService extends LazyLogging {
  def get[T](uri: Uri)(implicit
                       auth: HackeRankAuth,
                       backend: SttpBackend[Id, Nothing],
                       decoder: io.circe.Decoder[T]): Try[T] = {
    import HackeRankAuth._
    logger.debug(s"getting $uri")
    val response = sttp
      .authorize()
      .get(uri)
      .response(asJson[T])
      .send()

    logger.debug(s"got response ${response.getClass}")

    response.body match {
      case Right(Right(t)) =>
        logger.debug(s"success: ${t.getClass}")
        Success(t)
      case Right(Left(circeError)) =>
        logger.error(s"$circeError")
        Failure(circeError)
      case Left(s: String) =>
        logger.error(s)
        Failure(new Exception(s))
    }
  }

}
