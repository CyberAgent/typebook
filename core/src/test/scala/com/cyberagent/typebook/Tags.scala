package com.cyberagent.typebook

import org.scalatest.Tag

object SlowTest extends Tag("typebook.SlowTest")
object UnitTest extends Tag("typebook.UnitTest")
object IntegrationTest extends Tag("typebook.IntegrationTest")
object End2EndTest extends Tag("typebook.End2EndTest")
object MemoryIntensiveTest extends Tag("typebook.MemoryIntensiveTest")
object DockerExecTest extends Tag("typebook.DockerExecTest")
object DockerV11Test extends Tag("typebook.DockerV11Test")
object InDevelopmentTest extends Tag("typebook.InDevelopmentTest")
