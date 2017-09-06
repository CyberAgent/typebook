package com.cyberagent.typebook

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import scala.language.postfixOps

trait RuleFixture {

  def withRule[T <: TestRule](rule: T)(testCode: T => Any): Unit = {
    rule(
      new Statement() {
        override def evaluate(): Unit =  {
          testCode(rule)
        }
      },
      Description.createSuiteDescription("JUnit rule wrapper")
    ).evaluate()
  }

}
