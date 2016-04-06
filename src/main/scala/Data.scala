package ocscala 

object MagicOfSequence {
  import dispatch._
  import dispatch._, Defaults._ 
  import Data._
  import scala.concurrent.{ExecutionContext, Await, Future}
  import argonaut._, Argonaut._
  import scalaz._, Scalaz._

  /*
    import ocscala._
    import scala.concurrent.{ExecutionContext, Await, Future}
    import scala.concurent.duration._
    MagicOfSeqequence.getF  
  */
  val initialUser = "vmarquez"
  val usersUrl  = "https://api.github.com/users/"
  val organizationsURL = "http://api.github.com/"
  
  case class Credentials(user: String, password: String)
  
  def getGithubUser(userUrl: String, credentials: Credentials)(implicit ec: ExecutionContext): Future[\/[String, GithubUser]] = {
    println("usersURL = " + userUrl)
    val myurl = url(userUrl).as_!(credentials.user, credentials.password)
    Http(myurl OK as.String).map(str => str.decodeEither[GithubUser])
  }

  def getUsers(usersUrl: String, credentials: Credentials)(implicit ec: ExecutionContext): Future[\/[String, List[GithubUser]]] = {
    val myurl = url(usersUrl).as_!(credentials.user, credentials.password)
    Http(myurl OK as.String).map(str => str.decodeEither[List[GithubUser]])
  }

  def transform(users: List[String], credentials: Credentials): Future[\/[String, List[String]]] = {
    println("users = " + users)
    val futures: List[Future[\/[String, GithubUser]]] = users.map(userurl => getGithubUser(userurl, credentials)) //Future[\/[String, GithubUser]]
    val futureList: Future[List[\/[String, GithubUser]]] = flipMyFutures(futures) 
    futureList.map(list => {
      val eitherList: \/[String, List[GithubUser]] = list.sequenceU //from Scalaz if we have time, we'll implement it ourself here! 
      eitherList.map(l => l.map(user => user.name)) 
    })
  }

  def flipMyFutures[A](futures: List[Future[A]]): Future[List[A]] = ???
    //futures.foldLeft(Future { List[A]() })( WHAT GOES HERE?????  :-O

  //when we get a list of our followers, it doesn't give us their actual name! So we have to requery! let's do it concurrently. 
  def getNamesOfFollowers(login: String, password: String)(implicit ec: ExecutionContext): Future[\/[String, List[String]]] = {
    val credentials = Credentials(login, password) 
    (for {
      ghu       <- EitherT(getGithubUser(usersUrl + login, credentials))
      followers <- EitherT(getUsers(ghu.followers_url, credentials))
      urlList   = followers.map(u => usersUrl + u.login)
      names    <- EitherT(transform(urlList, credentials))
    } yield {
      names 
     }).run
  } 
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
  /*
   IGNORE EVERYTHING HERE.  I Just picked a JSON library, any will work! 
   */
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

 
  /* Some test data for playing in the REPL */
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
