package org.bitnot.hkwget.services

import com.softwaremill.sttp.{Empty, RequestT}
import com.typesafe.scalalogging.LazyLogging
import org.bitnot.hkwget.services.HackeRankAuth.NewRequest


trait HackeRankAuth {
  def setHeaders(req: NewRequest): NewRequest
}

object HackeRankAuth {
  type NewRequest = RequestT[Empty, String, Nothing]

  implicit class Authorizer(req: NewRequest)(implicit auth: HackeRankAuth) {
    def authorize(): NewRequest = auth.setHeaders(req)
  }

}

/*
// TODO: Figure out how auth actually works
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
}*/

case class FullCookieAuth(cookie: String)
  extends HackeRankAuth
    with LazyLogging {
  def setHeaders(req: NewRequest): NewRequest = {
    val CookieHeader = "Cookie"
    logger.trace(s"Authenticating with cookie")
    req.header(CookieHeader, cookie.stripLineEnd, true)
  }
}

case class SessionCookieAuth(sessionCookieValue: String)
  extends HackeRankAuth
    with LazyLogging {
  def setHeaders(req: NewRequest): NewRequest = {
    val CookieHeader = "Cookie"
    logger.trace(s"Authenticating with hrank_session cookie")
    req.header(CookieHeader, s"_hrank_session=${sessionCookieValue}", true)
  }
}
