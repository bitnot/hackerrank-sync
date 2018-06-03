package org.bitnot.hkwget.helpers

import java.time.ZonedDateTime

import org.bitnot.hkwget.models.hackerrank._

object CustomDecoders {
  import io.circe.java8.time.decodeZonedDateTimeDefault
  import io.circe.{Decoder, HCursor}

  implicit val decodeContestParticipation: Decoder[ContestParticipation] =
    new Decoder[ContestParticipation] {
      final def apply(c: HCursor): Decoder.Result[ContestParticipation] =
        for {
          contest_id <- c.downField("contest_id").as[Int]
          name <- c.downField("name").as[String]
          slug <- c.downField("slug").as[String]
          created_at <- c.downField("created_at").as[ZonedDateTime]
          contest_ended <- c.downField("contest_ended").as[Boolean]
          medal <- c.downField("medal").as[Option[String]]
        } yield {
          val total_participants =
            c.downField("total_participants").as[Int] match {
              case Right(id) => Some(id)
              case _         => None
            }

          val hacker_rank = c.downField("hacker_rank").as[Int] match {
            case Right(id) => Some(id)
            case _         => None
          }

          ContestParticipation(
            contest_id,
            name,
            slug,
            created_at,
            contest_ended,
            total_participants,
            hacker_rank,
            medal
          )
        }
    }

  implicit val decodeSubmission: Decoder[Submission] = new Decoder[Submission] {
    import io.circe.generic.auto._
    final def apply(c: HCursor): Decoder.Result[Submission] =
      for {
        id <- c.downField("id").as[Int]
        contest_id <- c.downField("contest_id").as[Int]
        challenge_id <- c.downField("challenge_id").as[Int]
        language <- c.downField("language").as[String]
        status <- c.downField("status").as[String]
        code <- c.downField("code").as[String]
        status <- c.downField("status").as[String]
        downloadable_test_cases <- c
          .downField("downloadable_test_cases")
          .as[Boolean]
        status_code <- c.downField("status_code").as[Int]
        name <- c.downField("name").as[String]
        slug <- c.downField("slug").as[String]
        challenge_slug <- c.downField("challenge_slug").as[String]
        contest_slug <- c.downField("contest_slug").as[String]
        track <- c.downField("track").as[Option[Track]]
      } yield {
        val display_score = c.downField("display_score").as[Double] match {
          case Right(score) => Some(score)
          case _            => None
        }

        Submission(
          id,
          contest_id,
          challenge_id,
          language,
          status,
          code,
          downloadable_test_cases,
          status_code,
          name,
          slug,
          challenge_slug,
          contest_slug,
          display_score,
          track
        )
      }
  }

}
