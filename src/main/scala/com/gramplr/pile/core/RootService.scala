package com.gramplr.pile.core

import akka.actor._
import spray.http._
import HttpMethods._
import spray.can.Http
import com.gramplr.pile.db.Database
import com.gramplr.pile.utils.Keygen
import spray.http.HttpHeaders.Location

class RootService extends Actor with Database with Config {

  lazy val baseurl = getString("url")

  def receive = {
    case _: Http.Connected => sender ! Http.Register(self)
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      sender ! HttpResponse(entity = "welcome to the pile service")
    case r@HttpRequest(GET, Uri.Path("/shorten"), _, _, _) => {
      val s = r.uri.query.getOrElse("url", "")
      val key = Keygen.generate

      insertShorten(key, s)
      sender ! HttpResponse(entity = baseurl + "/" + key)
    }
    case HttpRequest(GET, Uri.Path(path), _, _, _) => {
      val url = getURL(path.substring(1))

      sender ! HttpResponse(
        status = StatusCodes.TemporaryRedirect,
        headers = Location(url) :: Nil
      )
    }
  }

}
