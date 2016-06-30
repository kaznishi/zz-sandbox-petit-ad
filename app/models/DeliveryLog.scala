package models

import org.joda.time.DateTime
import play.api.Play
import slick.driver.H2Driver.api._
import com.github.tototoshi.slick.H2JodaSupport._

import scala.concurrent.Future
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

case class DeliveryLog(id: Option[Long], campaignId: Int, createdAt: DateTime)

trait DeliveryLogsTable {
  class DeliveryLogs(tag: Tag) extends Table[DeliveryLog](tag, "DELIVERY_LOGS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def campaignId = column[Int]("CAMPAIGN_ID")
    def createdAt = column[DateTime]("CREATED_AT", O.SqlType("DATETIME"))
    def * = (id.?, campaignId, createdAt) <> (DeliveryLog.tupled, DeliveryLog.unapply)
  }
  val deliveryLogs = TableQuery[DeliveryLogs]
}

object DeliveryLogsDAO extends HasDatabaseConfig[JdbcProfile] with DeliveryLogsTable {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def findById(id: Long): Future[Option[DeliveryLog]] =
    db.run(deliveryLogs.filter(_.id === id).result.headOption)

  def findAll: Future[Seq[DeliveryLog]] =
    db.run(deliveryLogs.result)

  /**
    * 指定キャンペーンのレコードを取得する
    * @param campaignId
    * @return
    */
  def findByCampaignId(campaignId: Int): Future[Seq[DeliveryLog]] =
    db.run(deliveryLogs.filter(_.campaignId === campaignId).result)

  /**
    * 指定日のレコードを取得する
    * @param dt 集計対象日(時刻は00:00:00.0のDateTime)
    * @return
    */
  def findByDate(dt: DateTime): Future[Seq[DeliveryLog]] = {
    val nextDt = dt.plusDays(1)
    db.run(deliveryLogs.filter(_.createdAt > dt).filter(_.createdAt < nextDt).result)
  }

  def insert(newRecord: DeliveryLog): Future[Long] = {
    db.run((deliveryLogs returning deliveryLogs.map(_.id)) += newRecord)
  }
}