package org.bitnot.hkwget.services

import java.net.URI
import java.nio.file.{Path, StandardOpenOption}

import com.typesafe.scalalogging.LazyLogging
import org.bitnot.hkwget.helpers.Urls
import org.bitnot.hkwget.models.{Challenge, Contest, Profile, local}

trait LocalStore {
  def save(profile: Profile)
}

class LocalFileStore(
                      outputDir: String,
                      overrideExisting: Boolean = false)
  extends LocalStore
    with LazyLogging {

  import java.nio.charset.StandardCharsets
  import java.nio.file.{Files, Paths}

  import LocalFileStore._

  override def save(profile: Profile): Unit = {
    val tableHeader = "Track | Topic | Score | Challenge" +
      "\n----- | ----- | ----- | ---------"
    logger.info("saving profile")
    for (contest <- profile.contests) {
      var index = List.empty[String]
      for (challenge <- contest.challenges) {
        val challengePath = challengeDirPath(contest, challenge)

        logger.debug(s"mkdir -p $challengePath")
        createDir(challengePath)

        saveStatement(contest, challenge)
        // TODO: Check downloadable_test_cases == true
        saveTestCases(contest, challenge)

        for (submission <- challenge.submissions) {
          try {
            saveSubmission(contest, challenge, submission)
          } catch {
            case ex: Throwable =>
              logger.error(s"Failed to save ${submission.id}", ex)
          }
        }

        val relativePath = challenge.track.map { case track =>
          Paths.get(
            track.parent_slug,
            track.slug,
            challenge.slug
          )
        }.getOrElse(
          Paths.get(
            challenge.slug
          )
        )

        val (trackParentSlug, trackSlug) = challenge.track
          .map(t => (t.parent_slug, t.slug))
          .getOrElse((" -/- ", " -/- "))
        index = s"${trackParentSlug}|${trackSlug}|${"%5.0f".format(challenge.score)}|[${challenge.slug}](./$relativePath/)" :: index
      }

      val contestIndexPath = filePathInContestDir(contest, "readme.md")

      val indexMd = s"# ${contest.slug}\n\n${tableHeader}\n${index.sorted.mkString("\n")}"

      Files.write(contestIndexPath,
        indexMd.getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING)
    }
  }

  private def filePathInContestDir(
                                    contest: Contest, fileName: String) = Paths.get(
    outputDir,
    contest.slug,
    fileName
  )

  private def challengeDirPath(
                                contest: Contest,
                                challenge: Challenge) =
    challenge.track.map { case track =>
      Paths.get(
        outputDir,
        contest.slug,
        track.parent_slug,
        track.slug,
        challenge.slug
      )
    }.getOrElse(
      Paths.get(
        outputDir,
        contest.slug,
        challenge.slug
      )
    )


  private def filePathInChallenge(
                                   contest: Contest,
                                   challenge: Challenge,
                                   fileName: String) =
    challenge.track.map { case track =>
      Paths.get(
        outputDir,
        contest.slug,
        track.parent_slug,
        track.slug,
        challenge.slug,
        fileName
      )
    }.getOrElse(
      Paths.get(
        outputDir,
        contest.slug,
        challenge.slug,
        fileName
      )
    )

  private def saveSubmission(contest: Contest,
                             challenge: Challenge,
                             submission: local.Submission): Unit = {
    val submissionPath = filePathInChallenge(
      contest,
      challenge,
      s"solution.${submission.language.fileExtension}")

    if (overrideExisting || !Files.exists(submissionPath)) {
      logger.debug(s"Writing submission: $submissionPath")
      Files.write(submissionPath,
        submission.sourceCode.getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING)
    } else logger.debug(s"File exists: $submissionPath")
  }

  private def saveStatement(contest: Contest, challenge: Challenge): Unit = {
    val statementPath = filePathInChallenge(contest, challenge, "statement.pdf")

    logger.debug(s"saving $statementPath")
    val statementUri = Urls.problemStatement(challenge.slug, contest.slug)

    downloadFileFromUrl(statementUri.toJavaUri, statementPath)
  }

  private def saveTestCases(contest: Contest, challenge: Challenge): Unit = {
    val testCasesPath = filePathInChallenge(contest, challenge, "testcases.zip")

    logger.debug(s"saving $testCasesPath")
    val testCasesUri = Urls.problemTestCases(challenge.slug, contest.slug)

    downloadFileFromUrl(testCasesUri.toJavaUri, testCasesPath)
  }

}

object LocalFileStore extends LazyLogging {

  import java.nio.file.Files

  def createDir(dirPath: Path): Unit = {
    if (Files.exists(dirPath)) {
      logger.debug(s"Directory exists: $dirPath")
    } else {
      logger.debug(s"Creating directory: $dirPath")
      try {
        Files.createDirectories(dirPath)
      } catch {
        case ex: Throwable => logger.error(s"Could not create $dirPath", ex)
      }
    }
  }

  import java.io.{FileOutputStream, IOException}
  import java.nio.channels.{Channels, ReadableByteChannel}
  import java.nio.file.{Files, Path}

  import scala.concurrent.blocking
  // This Method Is Used To Download A Sample File From The Url// This Method Is Used To Download A Sample File From The Url

  private def downloadFileFromUrl(fromUrl: URI, toFilePath: Path): Unit = {
    blocking {
      // Welcome to Java...
      if (Files.exists(toFilePath)) {
        logger.debug(s"File already exists: $toFilePath")
      } else {
        var rbcObj: ReadableByteChannel = null
        var fileOutputStream: FileOutputStream = null
        try {
          Files.createFile(toFilePath)
          rbcObj = Channels.newChannel(fromUrl.toURL.openStream)
          fileOutputStream = new FileOutputStream(toFilePath.toString)
          fileOutputStream.getChannel.transferFrom(rbcObj, 0, Long.MaxValue)
          logger.debug(s"File ${toFilePath} downloaded from ${fromUrl}")
        } catch {
          case ioEx: IOException =>
            logger.error(
              s"Problem downloading ${toFilePath} from ${fromUrl}", ioEx)
        } finally try {
          Option(fileOutputStream).foreach(_.close())
          Option(rbcObj).foreach(_.close())
        } catch {
          case ioEx: IOException =>
            logger.error(
              s"Problem closing ${toFilePath}", ioEx)
        }
      }
    }
  }
}
