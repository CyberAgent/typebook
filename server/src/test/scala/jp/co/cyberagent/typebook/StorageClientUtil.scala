package jp.co.cyberagent.typebook

import scala.language.postfixOps

import com.palantir.docker.compose.DockerComposeRule
import com.twitter.finagle.{Mysql => TMysql}
import com.twitter.finagle.mysql.{QueryRequest, Client => MysqlClient}
import com.twitter.util.{Await, Stopwatch}
import org.junit.rules.ExternalResource

import jp.co.cyberagent.typebook.config.BackendDbConfig
import jp.co.cyberagent.typebook.db.{Mysql, MySQLBackend, TableDefinition}


trait StorageClientUtil { self: StorageBackend =>

  private def waitUntilDbBecomesAvailable(conf: BackendDbConfig, tries: Int): Unit = {
    // wait util db is available using retryFilter
    // There is no way to apply this to RichClient
    val client = WaitUntilAvailable(tries) andThen TMysql.client
      .withCredentials(conf.user, conf.password.orNull)
      .withDatabase(conf.database)
      .newService(conf.servers)
    val elapsed = Stopwatch.start()
    Await.result(client(QueryRequest("SHOW TABLES;")))
    log.info(s"${elapsed().inSeconds} sec elapsed to be available")
    client.close()
  }

  // Create MySQL Client and wait until database is available
  def withDbClient(dc: DockerComposeRule, tries: Int = 10)(proc: MysqlClient => Any): Any = {
    val conf = BackendDbConfig(getStorageHostPort(dc))
    waitUntilDbBecomesAvailable(conf, tries)
    val client = Mysql.standard(conf)
    proc(client)
    client.close()
  }

  class TestMySqlBackend(client: MysqlClient) extends MySQLBackend {
    val mySqlClient: MysqlClient = client
  }
}



// Rule: prepare truncated tables before testing
case class UsingCleanTables(client: MysqlClient) extends ExternalResource {

  override def before(): Unit = {
    TableDefinition.initializeTables()(client)
    truncateAll(client)
  }

  private def truncateAll(client: MysqlClient): Unit = Await.result(
    client.query(s"TRUNCATE TABLE `${TableDefinition.Tables.Config.toString}`") join
      client.query(s"TRUNCATE TABLE `${TableDefinition.Tables.Schema.toString}`") flatMap { _ =>
      client.query(s"DELETE FROM `${TableDefinition.Tables.Subject.toString}`")
    }
  )
}
