package com.cyberagent.typebook

import org.slf4j.LoggerFactory

trait Logging extends Serializable {
  @transient lazy val log = LoggerFactory.getLogger(getClass.getName)
}
