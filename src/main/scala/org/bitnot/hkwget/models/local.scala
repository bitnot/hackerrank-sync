package org.bitnot.hkwget.models

import org.bitnot.hkwget.models.local.Submission
import org.bitnot.hkwget.models.hackerrank.{Submission => OnlineSubmission}


package object local {

  /*
  * $outDir/$contest/$challenge/solution.lang - latest accepted solution in lang
  * */

  case class Language(name: String,
                      fileExtension: String)

  case class Submission(id: Long,
                        slug: String,
                        sourceCode: String,
                        language: Language
                       )

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

    def apply(languageCode: String): Language = Language(languageCode, extensions.getOrElse(languageCode, languageCode))
  }

  object Submission {

    def apply(onlineSubmission: OnlineSubmission
             ): Submission =
      Submission(
        onlineSubmission.id, onlineSubmission.slug,
        onlineSubmission.code,
        Language(onlineSubmission.language)
      )
  }

}

case class Challenge(
                      slug: String,
                      submissions: Seq[Submission]
                    )

case class Contest(slug: String, challenges: Seq[Challenge])

case class Profile(
                    contests: Seq[Contest]
                  )

object Profile {

  def from(submissions: Seq[OnlineSubmission]): Profile = {
    def filterAccepted(submissions: Seq[OnlineSubmission]) = {
      submissions
        .filter(_.accepted)
    }

    def filterLatestByChallengeByLang(submissions: Seq[OnlineSubmission]) = {
      submissions
        .groupBy(s => (s.challenge_id, s.language))
        .map { case (_, submissions) =>
          submissions.maxBy(_.id)
        }
    }

    val latestAccepted = filterLatestByChallengeByLang(filterAccepted(submissions))
    val contests: Seq[Contest] = latestAccepted
      .groupBy(_.contest_slug)
      .map { case (contest_slug, submissions) =>
        val challenges = submissions
          .groupBy(_.challenge_slug)
          .map { case (challenge_slug, submissions) =>
            Challenge(challenge_slug, submissions.map(s => Submission(s)).toSeq)
          }.toSeq
        Contest(contest_slug, challenges)
      }.toSeq

    Profile(contests)
  }

}
