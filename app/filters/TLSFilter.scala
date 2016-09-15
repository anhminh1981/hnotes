package filters

import scala.concurrent.{ExecutionContext, Future}

import play.api.mvc.Filter
import play.api.mvc.Results
import scala.concurrent.Future
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import akka.stream.Materializer
import javax.inject.Inject

class TLSFilter @Inject()(
    implicit override val mat: Materializer,
    exec: ExecutionContext) extends Filter {
  def apply(nextFilter: RequestHeader => Future[Result])
    (requestHeader: RequestHeader): Future[Result] = {
      if(!requestHeader.secure)
        Future.successful(Results.MovedPermanently("https://" + requestHeader.host + requestHeader.uri))
      else
        nextFilter(requestHeader).map( _.withHeaders("Strict-Transport-Security" -> "max-age=31536000; includeSubDomains"))
  }
}