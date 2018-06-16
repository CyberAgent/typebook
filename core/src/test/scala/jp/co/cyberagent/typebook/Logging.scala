package jp.co.cyberagent.typebook

import org.slf4j.{Logger, LoggerFactory}

trait Logging extends Serializable {
  @transient lazy val log: Logger = LoggerFactory.getLogger(getClass.getName)
}
