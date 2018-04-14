package org.bitnot

package object hkwget {

  object JsonStubs {
    lazy val contests =
      scala.io.Source.fromResource("contests.json").getLines().mkString

    lazy val contestParticipations =
      scala.io.Source.fromResource("contestParticipations.json").getLines().mkString

    lazy val previews =
      scala.io.Source.fromResource("submissions.json").getLines().mkString

    lazy val singleSubmission =
      scala.io.Source.fromResource("submission.json").getLines().mkString

    lazy val languages =
      scala.io.Source.fromResource("languages.json").getLines().mkString
  }

}
