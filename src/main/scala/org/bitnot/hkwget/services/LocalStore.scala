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
    logger.info("saving profile")
    for (contest <- profile.contests) {
      for (challenge <- contest.challenges) {
        val challengePath = Paths.get(outputDir, contest.slug, challenge.slug)

        logger.debug(s"mkdir -p $challengePath")
        createDir(challengePath)

        saveStatement(contest, challenge)
        // TODO: Check downloadable_test_cases == true
        saveTestCases(contest, challenge)

        for (submission <- challenge.submissions) {
          try {
            saveSubmission(contest, submission)
          } catch {
            case ex: Throwable =>
              logger.error(s"Failed to save ${submission.id}", ex)
          }
        }
      }

    }
  }

  private def saveSubmission(contest: Contest,
                             submission: local.Submission): Unit = {
    val submissionPath = Paths.get(
      outputDir,
      contest.slug,
      submission.slug,
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
    val statementPath = Paths.get(
      outputDir,
      contest.slug,
      challenge.slug,
      "statement.pdf"
    )

    logger.debug(s"saving $statementPath")
    val statementUri = Urls.problemStatement(challenge.slug, contest.slug)

    downloadFileFromUrl(statementUri.toJavaUri, statementPath)
  }

  private def saveTestCases(contest: Contest, challenge: Challenge): Unit = {
    val testCasesPath =
      Paths.get(outputDir, contest.slug, challenge.slug, "testcases.zip")

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
