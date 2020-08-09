package org.bitnot.hkwget.services

import java.time.Duration

import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import org.bitnot.hkwget.helpers.CustomDecoders._
import org.bitnot.hkwget.helpers._
import org.bitnot.hkwget.models.hackerrank._

import scala.util._

trait HackerRankService {

  /**
    * Gets all programming languages supported by HR
    **/
  def getLanguages(): Try[LanguagesResponse]

  /**
    * Gets contests user has participated in
    **/
  def getContestParticipations(): Try[ApiResponse[ContestParticipation]]

  /**
    * Gets all submissions by user
    *
    * @param timeToLookBack           - how far back from now to look for submissions
    * @param maxSubmissionsPerContest - how many submissions to any contest to take
    **/
  def getSubmissions(
                      timeToLookBack: Option[Duration] = None,
                      maxSubmissionsPerContest: Option[Int] = None): Try[Seq[Submission]]
}


class HackerRankHttpService(username: String,
                            contestBlackList: Set[String] = Set.empty)(
                             implicit
                             auth: HackeRankAuth,
                             backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend())
  extends HackerRankService
    with LazyLogging {

  import HackerRankHttpService.get

  def getLanguages(): Try[LanguagesResponse] = get[LanguagesResponse](Urls.languages)

  override def getSubmissions(
                               timeToLookBack: Option[Duration] = None,
                               maxSubmissionsPerContestToSave: Option[Int] = None)
  : Try[Seq[Submission]] = {
    logger.info("running getSubmissions")

    val sinceUnixSeconds = timeToLookBack.map {
      case time => java.time.Instant.now.minus(time).toEpochMilli / 1000L
    }

    def trim(previews: Seq[SubmissionPreview]) =
      trimPreviews(previews, sinceUnixSeconds, maxSubmissionsPerContestToSave)


    val contestNames = "master" :: getContestParticipations.map { participation =>
      participation.models.collect {
        case x if x.hacker_rank.isDefined => x.slug
      }.toList
    }.getOrElse(List.empty)

    val whitelisted = contestNames.filterNot(contestBlackList.contains)
    Try {
      val previewsPerContest = whitelisted
        .map(
          contestName =>
            contestName -> getSubmissionPreviews(contestName)
              .map(_.models)
              .map(trim)
              .getOrElse(Seq.empty[SubmissionPreview])
        )
      previewsPerContest.flatMap {
        case (contestName, previews) =>
          previewsToSubmissions(contestName, previews)
      }
    }
  }

  private def trimPreviews(
                            previews: Seq[SubmissionPreview],
                            sinceUnixSeconds: Option[Long],
                            maxSubmissionsPerContestToSave: Option[Int]
                          ): Seq[SubmissionPreview] = {
    val solved = previews.filter(_.accepted)

    val dedupped = takeLatestByChallengeByLang(solved)

    val latest =
      sinceUnixSeconds
        .map(seconds => dedupped.filter(_.created_at >= seconds))
        .getOrElse(dedupped)

    val limited =
      maxSubmissionsPerContestToSave
        .map(limit => latest.take(limit))
        .getOrElse(latest) //TODO : limit total, not per contest
    limited.toSeq
  }

  private def previewsToSubmissions(
                                     contestName: String,
                                     previews: Seq[SubmissionPreview]
                                   ): Seq[Submission] = {
    logger.debug(s"${previews.size} previews  in ${contestName}")

    val submissions = previews.map { preview =>
      val maybeSubmission = getSubmission(contestName, preview)
      maybeSubmission
        .map(_.model)
        .toOption
    }.flatten
    submissions
  }

  private def getSubmission(contestName: String, preview: SubmissionPreview) = {
    get[SubmissionResponse](
      Urls.submission(
        preview.id,
        preview.challenge.slug,
        contest = contestName
      ))
  }

  def getContestParticipations(): Try[ApiResponse[ContestParticipation]] =
    get[ApiResponse[ContestParticipation]](Urls.contestParticipation(username))

  private def getSubmissionPreviews(
                                     contestName: String = "master"): Try[ApiResponse[SubmissionPreview]] =
    get[ApiResponse[SubmissionPreview]](
      Urls.submissions(contest = contestName, limit = 1000))

  private def takeLatestByChallengeByLang(submissions: Seq[SubmissionPreview]) =
    submissions
      .groupBy(s => (s.challenge, s.language))
      .values
      .map(_.maxBy(_.created_at))
}

object HackerRankHttpService extends LazyLogging {
  def get[T](uri: Uri)(implicit
                       auth: HackeRankAuth,
                       backend: SttpBackend[Id, Nothing],
                       decoder: io.circe.Decoder[T]): Try[T] = {
    import HackeRankAuth._
    logger.debug(s"getting $uri")
    val request = emptyRequest
      .header("User-Agent", "curl/7.54", true)
      .authorize()
      .get(uri)
      .response(asJson[T])

//    logger.debug(s"Request:\n${request.toCurl}")
    val response = request.send()

    response.body match {
      case Right(Right(t)) =>
        logger.debug(s"success: ${t.getClass}")
        Success(t)
      case Right(Left(circeError)) =>
        logger.error(s"$circeError")
        Failure(circeError.error)
      case Left(s: String) =>
        logger.error(s)
        Failure(new Exception(s))
    }
  }

}
