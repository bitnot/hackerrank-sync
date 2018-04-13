package org.bitnot.hkwget.services


import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.circe.java8.time._
import org.bitnot.hkwget.helpers._
import org.bitnot.hkwget.models.hackerrank._
import org.bitnot.hkwget.services.HackeRankAuth.NewRequest

import scala.util._


trait HackerRankService {

  def getLanguages(): Try[LanguagesResponse]

  def getContests: Try[ApiResponse[Contest]]

  def getSubmissions(maxSubmissionsToSave: Int = 1000): Try[Seq[Submission]]

  //todo: maybe ApiResponse[Submission] ?
}


trait HackeRankAuth {
  def setHeaders(req: NewRequest): NewRequest
}

object HackeRankAuth {

  type NewRequest = RequestT[Empty, String, Nothing]

  //val NewRequest = RequestT[Empty, String, Nothing]

  implicit class Authorizer(req: NewRequest)(implicit auth: HackeRankAuth) {
    def authorize(): NewRequest = auth.setHeaders(req)
  }

}

case class BasicHackeRankAuth(login: String, password: String)(
  implicit backend: SttpBackend[Id, Nothing]
) extends HackeRankAuth with LazyLogging {

  def setHeaders(
                  req: NewRequest): NewRequest = {

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

case class DummyHackeRankAuth(cookies: String) extends HackeRankAuth with LazyLogging {
  def setHeaders(req: NewRequest): NewRequest = {
    val CookieHeader = "Cookie"
    logger.info(s"Authenticating with cookie")
    req.header(CookieHeader, cookies)
  }
}


class HackerRankHttpService(auth: HackeRankAuth)
                           (implicit
                            backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
                           )
  extends HackerRankService with LazyLogging {

  import HackerRankHttpService.get

  implicit private val authorizer = auth

  def getLanguages(): Try[LanguagesResponse] =
    get[LanguagesResponse](Urls.languages)

  override def getSubmissions(maxSubmissionsToSave: Int = 1000): Try[Seq[Submission]] = {
    logger.info("running getSubmissions")
    for {
      previews: ApiResponse[SubmissionPreview] <- getSubmissionPreviews()
      if previews.models.nonEmpty

      contests: ApiResponse[Contest] <- getContests()
    } yield {

      val contestsMap = contests.models.map(c => c.id -> c.slug).toMap
      logger.debug(s"contestsMap ${contestsMap.size}")
      logger.debug(s"previews ${previews.total}")

      val acceptedPreviews = previews.models.filter(_.accepted)

      val latestByChallengeByLang = takeLatestByChallengeByLang(acceptedPreviews)
      val submissions = latestByChallengeByLang
        .map { preview =>
          val maybeSubmission = get[SubmissionResponse](
            Urls.submission(
              preview.id,
              preview.challenge.slug,
              contest = contestsMap.getOrElse(preview.contest_id, "master")
            ))
          maybeSubmission
            .map(_.model)
            .toOption
        }
        .flatten
        .toSeq
      submissions
    }
  }

  def getContests(): Try[ApiResponse[Contest]] =
    get[ApiResponse[Contest]](Urls.contests())

  private def getSubmissionPreviews() =
    get[ApiResponse[SubmissionPreview]](Urls.submissions(limit = 1000))

  private def takeLatestByChallengeByLang(submissions: Seq[SubmissionPreview]) = {
    submissions
      .groupBy(s => (s.challenge, s.language))
      .map { case ((_, _), submissions) =>
        submissions.maxBy(_.id)
      }
  }
}


object HackerRankHttpService extends LazyLogging {
  def get[T](uri: Uri)(implicit
                       auth: HackeRankAuth,
                       backend: SttpBackend[Id, Nothing],
                       decoder: io.circe.Decoder[T]): Try[T] = {
    import HackeRankAuth._
    logger.info(s"getting $uri")
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
