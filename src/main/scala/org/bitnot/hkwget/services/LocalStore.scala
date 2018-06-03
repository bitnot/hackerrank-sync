package org.bitnot.hkwget.services

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}

import com.typesafe.scalalogging.LazyLogging
import org.bitnot.hkwget.helpers.Urls
import org.bitnot.hkwget.models.{Challenge, Contest, Profile, local}

import scala.collection.JavaConverters._

trait LocalStore {
  def save(profile: Profile)
}

class LocalFileStore(outputDir: String, overrideExisting: Boolean = false)
    extends LocalStore
    with LazyLogging {
  import LocalFileStore._

  private final val tableHeader = "Track | Topic | Score | Challenge" +
    "\n----- | ----- | ----- | ---------"

  override def save(profile: Profile): Unit = {
    logger.info("saving profile")
    profile.contests.foreach(saveContest)
  }

  private def saveContest(contest: Contest) = {
    val saveChallenge: Challenge => Unit = saveChallengeFiles(_, contest)
    contest.challenges.foreach(saveChallenge)
    UpdateIndex(contest)
  }

  private def saveChallengeFiles(challenge: Challenge, contest: Contest) {
    val challengePath = challengeDirPath(contest, challenge)

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
  }

  private def UpdateIndex(contest: Contest) = {
    val index = contest.challenges.map { challenge =>
      val relativePath = challenge.track
        .map(
          track =>
            Paths.get(
              track.parent_slug,
              track.slug,
              challenge.slug
          ))
        .getOrElse(
          Paths.get(
            challenge.slug
          )
        )
      val (trackParentSlug, trackSlug) = challenge.track
        .map(t => (t.parent_slug, t.slug))
        .getOrElse((" -/- ", " -/- "))
      val scoreString = "%5.0f".format(challenge.score)
      s"$trackParentSlug|$trackSlug|$scoreString|[${challenge.slug}](./$relativePath/)"
    }

    val contestIndexPath = filePathInContestDir(contest, "readme.md")
    val existing =
      if (Files.exists(contestIndexPath)) {
        val lines = Files
          .readAllLines(contestIndexPath, StandardCharsets.UTF_8)
          .asScala
        lines.drop(4)
      } else Seq.empty

    val merged = (existing ++ index)
      .groupBy(identity)
      .keys
      .toSeq
      .sorted
    val indexMd =
      s"# ${contest.slug}\n\n$tableHeader\n${merged.mkString("\n")}"

    Files.write(contestIndexPath,
                indexMd.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)
  }

  private def filePathInContestDir(contest: Contest, fileName: String) =
    Paths.get(
      outputDir,
      contest.slug,
      fileName
    )

  private def challengeDirPath(contest: Contest, challenge: Challenge) =
    challenge.track
      .map(
        track =>
          Paths.get(
            outputDir,
            contest.slug,
            track.parent_slug,
            track.slug,
            challenge.slug
        ))
      .getOrElse(
        Paths.get(
          outputDir,
          contest.slug,
          challenge.slug
        )
      )

  private def filePathInChallenge(contest: Contest,
                                  challenge: Challenge,
                                  fileName: String) =
    challenge.track
      .map(
        track =>
          Paths.get(
            outputDir,
            contest.slug,
            track.parent_slug,
            track.slug,
            challenge.slug,
            fileName
        ))
      .getOrElse(
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

  import java.io.{FileOutputStream, IOException}
  import java.nio.channels.{Channels, ReadableByteChannel}
  import java.nio.file.{Files, Path}

  import scala.concurrent.blocking

  def createDir(dirPath: Path): Unit = {
    logger.debug(s"mkdir -p $dirPath")
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
          logger.debug(s"File $toFilePath downloaded from $fromUrl")
        } catch {
          case ioEx: IOException =>
            logger.error(s"Problem downloading $toFilePath from $fromUrl", ioEx)
        } finally try {
          Option(fileOutputStream).foreach(_.close())
          Option(rbcObj).foreach(_.close())
        } catch {
          case ioEx: IOException =>
            logger.error(s"Problem closing $toFilePath", ioEx)
        }
      }
    }
  }
}
