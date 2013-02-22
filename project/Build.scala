import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "AmararegameTracker"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "org.twitter4j" % "twitter4j-core" % "3.0.3",
      "org.twitter4j" % "twitter4j-stream" % "3.0.3"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
