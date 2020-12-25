package org.bitnot.hkwget.models

import org.bitnot.hkwget.models.hackerrank.{Submission => OnlineSubmission}
import org.bitnot.hkwget.models.local.Submission

package object local {

  /*
   * $outDir/$contest/$challenge/solution-$id.lang - accepted solution in lang
   * */

  case class Language(name: String, fileExtension: String)

  case class Submission(id: Long,
                        slug: String,
                        sourceCode: String,
                        language: Language){
    val fileName = s"solution-${id}.${language.fileExtension}"
  }

  object Language {
    // todo: make dynamic
    // todo: fix extensions
    val extensions = Map(
      "ada" -> "ada",
      "babeljs" -> "js",
      "bash" -> "sh",
      "brainfuck" -> "bf",
      "c" -> "c",
      "c_clang" -> "c",
      "clojure" -> "clj",
      "cobol" -> "cobol",
      "coffeescript" -> "coffee",
      "cpp" -> "cpp",
      "cpp14" -> "cpp",
      "cpp_clang" -> "cpp",
      "csharp" -> "cs",
      "d" -> "d",
      "db2" -> "db2",
      "elixir" -> "ex",
      "erlang" -> "erl",
      "fortran" -> "f03",
      "fsharp" -> "fs",
      "go" -> "go",
      "groovy" -> "groovy",
      "haskell" -> "hs",
      "haxe" -> "haxe",
      "java" -> "java",
      "java8" -> "java",
      "javascript" -> "js",
      "julia" -> "julia",
      "kotlin" -> "kt",
      "lolcode" -> "lolcode",
      "lua" -> "lua",
      "mysql" -> "sql",
      "objectivec" -> "m",
      "ocaml" -> "ocaml",
      "octave" -> "octave",
      "oracle" -> "sql",
      "pascal" -> "ps",
      "perl" -> "perl",
      "php" -> "psp",
      "pypy" -> "py",
      "pypy3" -> "py",
      "python" -> "py",
      "python3" -> "py",
      "r" -> "r",
      "racket" -> "racket",
      "ruby" -> "rb",
      "rust" -> "rust",
      "sbcl" -> "sbcl",
      "scala" -> "scala",
      "smalltalk" -> "smalltalk",
      "swift" -> "swift",
      "tcl" -> "swift",
      "text" -> "text",
      "text_pseudo" -> "text",
      "tsql" -> "sql",
      "typescript" -> "ts",
      "visualbasic" -> "vb",
      "whitespace" -> "whitespace",
      "xquery" -> "xquery"
    )

    def apply(languageCode: String): Language =
      Language(languageCode, extensions.getOrElse(languageCode, languageCode))
  }

  object Submission {

    def apply(onlineSubmission: OnlineSubmission): Submission =
      Submission(
        onlineSubmission.id,
        onlineSubmission.slug,
        onlineSubmission.code,
        Language(onlineSubmission.language)
      )
  }

}

case class Challenge(
    slug: String,
    submissions: Seq[Submission],
    track: Option[Track],
    score: Double
)

case class Track(slug: String, parent_slug: String)

case class Contest(slug: String, challenges: Seq[Challenge])

class Profile(
    val contests: Seq[Contest]
)

object Profile {

  def apply(submissions: Seq[OnlineSubmission]): Profile = {
    val solved = submissions.filter(_.accepted)
    val latest = solved
      .groupBy(s => (s.challenge_id, s.language))
      .values
      .map(_.maxBy(_.id))
    val contests = latest
      .groupBy(_.contest_slug)
      .map((toContest _).tupled)
      .toSeq

    new Profile(contests)
  }

  private def toContest(contest_slug: String,
                        submissions: Iterable[OnlineSubmission]): Contest = {
    val challenges = submissions
      .groupBy(_.challenge_slug)
      .map((toChallenge _).tupled)
      .toSeq
    Contest(contest_slug, challenges)
  }

  private def toChallenge(
      challenge_slug: String,
      submissions: Iterable[OnlineSubmission]): Challenge = {
    //note: `submissions` cannot be empty, since is a result of `groupBy`
    val top = submissions.head
    val track = top.track.map(t => Track(t.slug, t.track_slug))
    val score = top.display_score.getOrElse(0.0)
    Challenge(
      challenge_slug,
      submissions.map(Submission.apply).toSeq,
      track,
      score
    )
  }
}
