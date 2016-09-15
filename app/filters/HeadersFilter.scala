package filters

import akka.stream.Materializer
import play.api.mvc.Filter
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.Logger

class HeadersFilter @Inject()(
    implicit override val mat: Materializer,
    exec: ExecutionContext) extends Filter {
  
  def apply(nextFilter: RequestHeader => Future[Result])
    (requestHeader: RequestHeader): Future[Result] = {
    requestHeader.headers.headers.map { case (key, value) =>
      Logger.info(s"HEADER: $key: $value")
    }
    nextFilter(requestHeader)
  }
}