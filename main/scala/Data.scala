package ocscala 

object MagicOfSequence {
  import dispatch._
  import dispatch._, Defaults._ 
  import Data._
  import scala.concurrent.{ExecutionContext, Await, Future}
  import argonaut._, Argonaut._
  import scalaz._, Scalaz._

  val initialUser = "vmarquez"
  val usersUrl  = "https://api.github.com/users/"
  val organizationsURL = "http://api.github.com/"

  def getGithubUser(user: String)(implicit ec: ExecutionContext): Future[\/[String, GithubUser]] = {
    Http(url(usersUrl + user) OK as.String).map(str => str.decodeEither[GithubUser])
  }

  def getUsers(usersUrl: String)(implicit ec: ExecutionContext): Future[\/[String, List[GithubUser]]] =
    Http(url(usersUrl) OK as.String).map(str => str.decodeEither[List[GithubUser]])

  def transform(users: List[String]): Future[\/[String, List[String]]] = {
    val futures: List[Future[\/[String, GithubUser]]] = users.map(loginName => getGithubUser(loginName)) //Future[\/[String, GithubUser]]
    val futureList: Future[List[\/[String, GithubUser]]] = flipMyFutures(futures) 
    futureList.map(list => {
      val eitherList: \/[String, List[GithubUser]] = list.sequenceU //from Scalaz 
      eitherList.map(l => l.map(user => user.login)) 
    })
  }

  def flipMyFutures[A](futures: List[Future[A]]): Future[List[A]] = ???
    //futures.foldLeft(Future { List[A]() })( WHAT GOES HERE?????  :-O

  def transformersGetNamesOfFollowers(login: String)(implicit ec: ExecutionContext): Future[\/[String, List[String]]] =
    (for {
      ghu       <- EitherT(getGithubUser(login))
      followers <- EitherT(getUsers(ghu.followers_url))
      urlList   = followers.map(u => usersUrl + u.login)
      names    <- EitherT(transform(urlList))
    } yield {
      names 
     }).run
 
}

object Data {

  case class GithubUser(
    login: String, 
    id: Int,
    avatar_url: String,
    url: String,
    html_url: String,
    followers_url: String,
    following_url: String,
    name: String)

  import argonaut._
  import Argonaut._
  implicit def decodeGithubUser: DecodeJson[GithubUser] = {
    DecodeJson(c => for {
      login		    <- (c --\ "login").as[String]
      id		            <- (c --\ "id").as[Int]
      avatar_url		    <- (c --\ "avatar_url").as[String]
      url		            <- (c --\ "url").as[String]
      html_url		      <- (c --\ "html_url").as[String]
      followers_url		  <- (c --\ "followers_url").as[String]
      following_url		  <- (c --\ "following_url").as[String]
      email		        <- (c --\ "email").as[Option[String]]
      name		        <- (c --\ "name").as[Option[String]]
    } yield GithubUser(
            login, 
            id,
            avatar_url,
            url,
            html_url,
            followers_url,
            following_url,
            name.getOrElse(""))
    )
  } 

 

  val followersFollowingData = """
 [{
    "login": "jmk",
    "id": 177193,
    "avatar_url": "https://avatars.githubusercontent.com/u/177193?v=3",
    "gravatar_id": "",
    "url": "https://api.github.com/users/jmk",
    "html_url": "https://github.com/jmk",
    "followers_url": "https://api.github.com/users/jmk/followers",
    "following_url": "https://api.github.com/users/jmk/following{/other_user}",
    "gists_url": "https://api.github.com/users/jmk/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/jmk/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/jmk/subscriptions",
    "organizations_url": "https://api.github.com/users/jmk/orgs",
    "repos_url": "https://api.github.com/users/jmk/repos",
    "events_url": "https://api.github.com/users/jmk/events{/privacy}",
    "received_events_url": "https://api.github.com/users/jmk/received_events",
    "type": "User",
    "site_admin": false
  },
  {
    "login": "dainkaplan",
    "id": 594193,
    "avatar_url": "https://avatars.githubusercontent.com/u/594193?v=3",
    "gravatar_id": "",
    "url": "https://api.github.com/users/dainkaplan",
    "html_url": "https://github.com/dainkaplan",
    "followers_url": "https://api.github.com/users/dainkaplan/followers",
    "following_url": "https://api.github.com/users/dainkaplan/following{/other_user}",
    "gists_url": "https://api.github.com/users/dainkaplan/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/dainkaplan/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/dainkaplan/subscriptions",
    "organizations_url": "https://api.github.com/users/dainkaplan/orgs",
    "repos_url": "https://api.github.com/users/dainkaplan/repos",
    "events_url": "https://api.github.com/users/dainkaplan/events{/privacy}",
    "received_events_url": "https://api.github.com/users/dainkaplan/received_events",
    "type": "User",
    "site_admin": false
  }]  
  """
  val testFullUserData = """
    {
      "login": "vmarquez",
      "id": 427578,
      "avatar_url": "https://avatars.githubusercontent.com/u/427578?v=3",
      "gravatar_id": "",
      "url": "https://api.github.com/users/vmarquez",
      "html_url": "https://github.com/vmarquez",
      "followers_url": "https://api.github.com/users/vmarquez/followers",
      "following_url": "https://api.github.com/users/vmarquez/following{/other_user}",
      "gists_url": "https://api.github.com/users/vmarquez/gists{/gist_id}",
      "starred_url": "https://api.github.com/users/vmarquez/starred{/owner}{/repo}",
      "subscriptions_url": "https://api.github.com/users/vmarquez/subscriptions",
      "organizations_url": "https://api.github.com/users/vmarquez/orgs",
      "repos_url": "https://api.github.com/users/vmarquez/repos",
      "events_url": "https://api.github.com/users/vmarquez/events{/privacy}",
      "received_events_url": "https://api.github.com/users/vmarquez/received_events",
      "type": "User",
      "site_admin": false,
      "name": "Vincent Marquez",
      "company": null,
      "blog": "http://www.twitter.com/runT1ME",
      "location": "Irvine, CA",
      "email": "vincent dot marquez at gmail dot com",
      "hireable": null,
      "bio": null,
      "public_repos": 20,
      "public_gists": 22,
      "followers": 13,
      "following": 14,
      "created_at": "2010-10-05T06:35:10Z",
      "updated_at": "2016-03-31T22:31:32Z"
  } 
  """
}
