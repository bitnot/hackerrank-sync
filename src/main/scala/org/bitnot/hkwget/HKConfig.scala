package org.bitnot.hkwget

import com.typesafe.config.Config


case class HKConfig(
                     contestBlackList: Seq[String],
                     outputDir: String,
                     maxPerContest: Int,
                     timeToLookBack: java.time.Duration,
                     login: String
                   )

object HKConfig {
  import scala.collection.JavaConverters._

  def apply(hkwgetConfig: Config
           ): HKConfig = new HKConfig(

    hkwgetConfig.getStringList("contestBlackList").asScala,
    hkwgetConfig.getString("outputDir"),
    hkwgetConfig.getInt("maxSubmissionsPerContestToSave"),
    hkwgetConfig.getDuration("timeToLookBack"),
    hkwgetConfig.getString("auth.login")
  )
}