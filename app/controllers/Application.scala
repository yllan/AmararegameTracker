package controllers

import play.api._
import libs.iteratee.{Concurrent, Enumeratee}
import libs.ws.WS
import play.api.mvc._
import twitter4j._
import scala.concurrent.ExecutionContext. Implicits.global

object Application extends Controller {

  val (amazonEnumerator, amazonChannel) = Concurrent.broadcast[String]

  val listener = new UserStreamListener {
    def onStallWarning(p1: StallWarning) {}

    def onException(p1: Exception) {}

    def onDeletionNotice(p1: StatusDeletionNotice) {}

    def onScrubGeo(p1: Long, p2: Long) {}

    def onStatus(p1: twitter4j.Status) {
      Logger.info(p1.getUser.getScreenName)

      val urlExtractor = """(?s).*(http://[\w\d\./]+).*""".r
      if (p1.getUser.getScreenName == "amararegame" && !p1.getText.contains("終了")) {
        val beginTime = new java.util.Date().getTime
        p1.getText match {
          case urlExtractor(url) => {
            WS.url(url).withFollowRedirects(true).get.onSuccess {
              case response: play.api.libs.ws.Response => {
                val body = response.getAHCResponse.getResponseBody("UTF-8")
                Logger.info("Redirect Cost: " + (new java.util.Date().getTime - beginTime))
                val amazonURLExtractor = """(?s).*<a href="([^\"]+)">こちら</a>.*""".r
                body match {
                  case amazonURLExtractor(amazonURL) => {
                    Logger.info(amazonURL)
                    if (p1.getText.contains("ジョジョ")) amazonChannel.push(amazonURL)
                  }
                }
              }
            }
          }
          case _ => println("No match URL!")
        }
      }
    }

    def onTrackLimitationNotice(p1: Int) {}

    def onDeletionNotice(p1: Long, p2: Long) {}

    def onFriendList(p1: Array[Long]) {}

    def onFavorite(p1: User, p2: User, p3: twitter4j.Status) {}

    def onUnfavorite(p1: User, p2: User, p3: twitter4j.Status) {}

    def onFollow(p1: User, p2: User) {}

    def onDirectMessage(p1: DirectMessage) {}

    def onUserListMemberAddition(p1: User, p2: User, p3: UserList) {}

    def onUserListMemberDeletion(p1: User, p2: User, p3: UserList) {}

    def onUserListSubscription(p1: User, p2: User, p3: UserList) {}

    def onUserListUnsubscription(p1: User, p2: User, p3: UserList) {}

    def onUserListCreation(p1: User, p2: UserList) {}

    def onUserListUpdate(p1: User, p2: UserList) {}

    def onUserListDeletion(p1: User, p2: UserList) {}

    def onUserProfileUpdate(p1: User) {}

    def onBlock(p1: User, p2: User) {}

    def onUnblock(p1: User, p2: User) {}

  }

  val twitterStream = new TwitterStreamFactory().getInstance()
  twitterStream.addListener(listener)
  twitterStream.user()

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  val toEventSource = Enumeratee.map[String] { url =>
    "event: redirect\n" +
    "data: " + url + "\n\n"
  }

  def amazonStream = Action {
    Ok.feed(amazonEnumerator &> toEventSource).as("text/event-stream")
  }
  
}