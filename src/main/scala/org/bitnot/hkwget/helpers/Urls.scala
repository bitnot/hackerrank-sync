package org.bitnot.hkwget.helpers

object Urls {

  import com.softwaremill.sttp._

  /**
    * Endpoint accepting POST for authentication
    **/
  def login: Uri = uri"https://www.hackerrank.com/auth/login"

  /**
    * Code for a particular submission
    **/
  def submission(id: Int,
                 challenge: String,
                 contest: String = "master",
                 offset: Int = 0,
                 limit: Int = 1000) =
    uri"https://www.hackerrank.com/rest/contests/$contest/challenges/$challenge/submissions/$id"

  /**
    * Submissions to a problem
    **/
  def problemSubmissions(challenge: String,
                         contest: String = "master",
                         offset: Int = 0,
                         limit: Int = 1000) =
    uri"https://www.hackerrank.com/rest/contests/$contest/challenges/$challenge/submissions/?offset=$offset&limit=$limit"


  def problemStatement(challenge: String,
                       contest: String = "master") =
    uri"https://www.hackerrank.com/rest/contests/${contest}/challenges/${challenge}/download_pdf?language=English"


  def problemTestCases(challenge: String,
                       contest: String = "master") =
    uri"https://www.hackerrank.com/rest/contests/${contest}/challenges/${challenge}/download_testcases"


  /**
    * List of submissions:
    **/
  def submissions(contest: String = "master",
                  offset: Int = 0,
                  limit: Int = 1000) =
    uri"https://www.hackerrank.com/rest/contests/$contest/submissions/?offset=$offset&limit=$limit"

  /**
    * List of contests:
    **/
  def contests(offset: Int = 0, limit: Int = 1000) =
    uri"https://www.hackerrank.com/rest/contests/upcoming?offset=$offset&limit=$limit&contest_slug=active"

  /**
    * List of languages:
    **/
  def languages = uri"http://api.hackerrank.com/checker/languages.json"
}
