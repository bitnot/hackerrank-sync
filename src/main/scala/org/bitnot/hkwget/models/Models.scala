package org.bitnot.hkwget.models


package object hackerrank {

  import java.time.ZonedDateTime

  /*
TODO:make use of @ConfiguredJsonCodec
import CirceConfig._
object CirceConfig {
  implicit val circeConfig = Configuration.default.withSnakeCaseMemberNames
}
*/


  case class Credentials(login: String, password: String, rememberMe: Boolean = false)


  case class Languages(names: Map[String, String],
                       codes: Map[String, Int])

  case class LanguagesResponse(languages: Languages)


  case class Contest(id: Int,
                     name: String,
                     slug: String,
                     created_at: String,
                     rated: Boolean,
                     started: Boolean,
                     ended: Boolean,
                     epoch_endtime: Int,
                     epoch_starttime: Int,
                     time_left: Double,
                     description_html: String,
                     get_starttimeiso: ZonedDateTime,
                     get_endtimeiso: ZonedDateTime,
                     challenges_count: Option[Int],
                     // has_codesprint_reg_page: Any,
                     // categories: Seq[Any],
                     // domain_restrictions: Any,
                     // additional_details_required: Any,
                     // skip_signup: Any,
                     archived: Boolean,
                     // track: Any,
                     // has_tracks: Any,
                     description: String,
                     // leaderboard_broadcast_message: Any,
                     is_multi_round: Boolean,
                     // parent_contest_id: Any,
                     // qualification_rule_type: Any,
                     qualification_rule_value: Int,
                     // qualification_rule_msg: Any,
                     limited_participants: Option[Boolean],
                     // migration_status: Any,
                     // migration_disabled: Any,
                     ask_jobs_profile: Boolean,
                     // organization_type: Any,
                     // organization_name: Any,
                     // india_codesprint: Any,
                     // ask_role_number: Any,
                     // hide_ask_resume: Any,
                     // ask_gender: Any,
                     // ask_major: Any,
                     effective_time_left: Double,
                     effective_epoch_endtime: Int,
                     signed_up: Boolean,
                     // moderator: Boolean,
                     visible: Boolean,
                     additional_details_filled: Boolean

                    )


  case class ApiResponse[T](models: Seq[T],
                            total: Int,
                            page: Option[Int]
                           )

  case class Track(


                    id: Int,
                    name: String,
                    slug: String,
                    track_id: Int,
                    track_name: String,
                    track_slug: String,
                    // rewards_system_enabled: Option[Any],
                    taxonomy: Int


                  )

  case class ChallengeName(
                            name: String,
                            slug: String
                          )

  case class Challenge(
                        url: String,
                        name: String,
                        slug: String,
                        preview: String,
                        solved_count: Int,
                        total_count: Int,
                        max_score: Int,
                        difficulty_name: String
                      )

  case class SubmissionPreview(

                                id: Int,
                                challenge_id: Int,
                                contest_id: Int,
                                hacker_id: Int,
                                status: String,
                                kind: String,
                                created_at: Int,
                                language: String,
                                //      hacker_username: Option[Any],
                                time_ago: String,
                                in_contest_bounds: Boolean,
                                status_code: Int,
                                score: String,
                                //      is_preliminary_score: Option[Any],
                                challenge: ChallengeName,
                                inserttime: Int

                              )


  case class Submission(
                         id: Int,
                         contest_id: Int,
                         challenge_id: Int,
                         hacker_id: Int,
                         language: String,
                         kind: String,
                         status: String,
                         language_status: Int,
                         score_processed: Int,
                         score: String,
                         solved: Int,
                         partial: Int,
                         mu: String,
                         variance: String,
                         code: String,
                         compile_status: Int,
                         compile_message: String,
                         testcase_status: Seq[Int],
                         testcase_message: Seq[String],
                         //          stderr: Option[Any],
                         codechecker_signal: Seq[Int],
                         codechecker_time: Seq[Double],
                         //          finishtime: Option[Any],
                         created_at: String,
                         updated_at: String,
                         //          is_preliminary_score: Option[Any],
                         //          test_weights: Option[Any],
                         //          custom_challenge_config: Option[Any],
                         downloadable_test_cases: Boolean,
                         status_code: Int,
                         //          live_status: Map[String, Any],
                         name: String,
                         slug: String,
                         custom: Boolean,
                         is_custom: Boolean,
                         challenge_slug: String,
                         //          company: Option[Any],
                         contest_slug: String,
                         created_at_epoch: String,
                         player_count: Int,
                         is_editorial_available: Boolean,
                         display_score: String,
                         //          free_test_cases: Seq[Any],
                         codechecker_hash: String,
                         progress: Int,
                         progress_states: Int,
                         track: Track,
                         is_sample_testcase: Seq[Boolean],
                         is_additional_testcase: Seq[Boolean],
                         individual_test_case_score: Seq[Double],
                         unlocked_challenges: String,
                         next_challenge: Challenge,
                         next_challenge_slug: String,
                         random_challenge_slug: String
                       ){
    def accepted: Boolean = status == "Accepted"
  }

  case class SubmissionResponse(model: Submission)

}