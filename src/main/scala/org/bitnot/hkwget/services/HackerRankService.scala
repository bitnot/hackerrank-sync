package org.bitnot.hkwget.services

import java.time.Duration
import sttp.client._
import sttp.client.circe._
import org.bitnot.hkwget.helpers.LazyLogging
import io.circe
import io.circe.generic.auto._
import org.bitnot.hkwget.helpers.CustomDecoders._
import org.bitnot.hkwget.helpers._
import org.bitnot.hkwget.models.hackerrank._
import sttp.model.Uri

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
                             auth: HackerRankAuth,
                             backend: SttpBackend[Identity, Nothing, NothingT]
                             ) extends HackerRankService
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


    val contestNames = "master" :: getContestParticipations().map { participation =>
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
    logger.debug(s"Trimming ${previews.size} previews since ${
      sinceUnixSeconds.map(_.toString).getOrElse("ever")
    } up to ${
      maxSubmissionsPerContestToSave.map(_.toString).getOrElse("everything")
    }")
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
    logger.debug(s"${previews.size} previews in ${contestName}")

    val submissions = previews.flatMap { preview =>
      val maybeSubmission = getSubmission(contestName, preview)
      maybeSubmission
        .map(_.model)
        .toOption
    }
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
                       auth: HackerRankAuth,
                       backend: SttpBackend[Identity, Nothing, NothingT],
                       decoder: io.circe.Decoder[T]): Try[T] = {
    import HackerRankAuth._
    logger.debug(s"getting $uri")
    val request = emptyRequest
      .header("User-Agent", "curl/7.67", true)
      .authorize()
      .get(uri)
      .response(asJson[T])

    //    logger.debug(s"Request:\n${request.toCurl}")
    val response: Identity[Response[Either[ResponseError[circe.Error], T]]] = request.send()
    val body: Either[ResponseError[circe.Error], T] = response.body
    response.body match {
      case Right(t) =>
        logger.debug(s"success: ${t.getClass}")
        Success(t)
      case Left(circeError) =>
        logger.error(s"$circeError")
        Failure(circeError.getCause)
    }
  }

}
