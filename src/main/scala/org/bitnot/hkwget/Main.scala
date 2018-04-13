package org.bitnot.hkwget

object Main extends App {

  import com.typesafe.config.ConfigFactory
  import io.circe.java8.time._


//  val authConf = ConfigFactory.load().getConfig("hkwget.auth")
//  val login = authConf.getString("login")
//  val password = authConf.getString("password")


  val cookieTxt =
    scala.io.Source.fromResource("cookies.txt").getLines().mkString

  val hkService = new HackerRankHttpService(DummyHackeRankAuth(cookieTxt))

  val submissions = hkService.getSubmissions
  //  import io.circe.parser.decode
  //  val json = scala.io.Source.fromResource("submissions.json").getLines.mkString
  //  val submissions = decode[ApiResponse[SubmissionPreview]](json)
  println(s"#submissions: ${submissions}")

}
