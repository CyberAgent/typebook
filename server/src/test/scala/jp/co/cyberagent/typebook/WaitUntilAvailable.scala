package jp.co.cyberagent.typebook

import scala.language.postfixOps

import com.twitter.conversions.time._
import com.twitter.finagle.service.{RetryBudget, RetryExceptionsFilter, RetryPolicy}
import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.finagle.util.DefaultTimer

object WaitUntilAvailable {
  // This is RetryFilter for avoiding ChannelClosedException when using dockerCompose on testing
  def apply[Req, Rep](tries: Int = 10): RetryExceptionsFilter[Req, Rep] = new RetryExceptionsFilter(
    retryPolicy = RetryPolicy.backoff(Stream.fill(tries)(5 second))(RetryPolicy.ChannelClosedExceptionsOnly).limit(tries),
    timer = DefaultTimer,
    statsReceiver = NullStatsReceiver,
    retryBudget = RetryBudget.Infinite
  )
}
