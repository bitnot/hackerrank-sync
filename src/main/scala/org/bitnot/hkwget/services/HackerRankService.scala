package org.bitnot.hkwget.services


import scala.util._

import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import io.circe.generic.auto._
import io.circe.java8.time._

import org.bitnot.hkwget.models.hackerrank._
import org.bitnot.hkwget.helpers._
import org.bitnot.hkwget.services.HackeRankAuth.NewRequest


trait HackerRankService {

  def getLanguages(): Try[LanguagesResponse]

  def getContests: Try[ApiResponse[Contest]]

  def getSubmissions
  : Try[Seq[Submission]] //todo: maybe ApiResponse[Submission] ?
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
) extends HackeRankAuth {

  def setHeaders(
                  req: NewRequest): NewRequest = {

    println(s"Authenticating $login")
    val loginResponse =
      sttp
        .post(Urls.login)
        .body(Credentials(login, password))
        .send()
    println(loginResponse)
    println(loginResponse.cookies)
    println(sttp.cookies(loginResponse))
    val CookieHeader = "Cookie"

    val cookies =
      loginResponse.cookies.map(p => p.name + "=" + p.value).mkString("; ")
    // TODO: FIX ME
    req.header(CookieHeader, cookies)
  }
}

case class DummyHackeRankAuth(cookies: String) extends HackeRankAuth {
  def setHeaders(req: NewRequest): NewRequest = {

    val CookieHeader = "Cookie"
    println(s"Authenticating with cookie")
    req.header(CookieHeader, cookies)
  }
}

class HackerRankHttpService(auth: HackeRankAuth) extends HackerRankService {

  import HackerRankHttpService.get

  implicit private val authorizer = auth

  def getLanguages(): Try[LanguagesResponse] =
    get[LanguagesResponse](Urls.languages)

  def getContests(): Try[ApiResponse[Contest]] =
    get[ApiResponse[Contest]](Urls.contests())

  private def getSubmissionPreviews() =
    get[ApiResponse[SubmissionPreview]](Urls.submissions(limit = 10))

  override def getSubmissions: Try[Seq[Submission]] = {
    println("running getSubmissions")
    for {
      previews: ApiResponse[SubmissionPreview] <- getSubmissionPreviews()
      if previews.models.nonEmpty

      contests: ApiResponse[Contest] <- getContests()
    } yield {

      val contestsMap = contests.models.map(c => c.id -> c.slug).toMap
      println(s"contestsMap $contestsMap")
      println(s"previews $previews")

      val submissions =
        previews.models
          .take(10)
          .map { preview =>
            val maybeSubmission = get[SubmissionResponse](
              Urls.submission(
                preview.id,
                preview.challenge.slug,
                contest = contestsMap.getOrElse(preview.contest_id, "master")
              ))
            println(maybeSubmission)
            maybeSubmission
              .map(_.model)
              .toOption
          }
          .flatten
      submissions
    }
  }
}

object HackerRankHttpService {
  implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()

  def get[T](uri: Uri)(implicit
                       auth: HackeRankAuth,
                       decoder: io.circe.Decoder[T]): Try[T] = {
    import HackeRankAuth._
    println(s"getting $uri")
    val response = sttp
      .authorize()
      .get(uri)
      .response(asJson[T])
      .send()

    println(s"got response $response")

    val debugResponse = sttp
      .authorize()
      .get(uri)
      .send()
    println(s"got debugResponse: ${debugResponse.unsafeBody}")

    response.body match {
      // TODO: deal with erasure
      case Right(Right(t: T)) =>
        println(s"success: $t")
        Success(t)
      case Right(Left(circeError)) =>
        println(circeError)
        Failure(circeError)
      case Left(s: String) =>
        println(s)
        Failure(new Exception(s))
    }

  }
}
