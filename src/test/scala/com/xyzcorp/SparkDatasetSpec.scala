package com.xyzcorp

import java.net.URL

import org.apache.spark.SparkConf
import org.apache.spark.sql._
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.io.StdIn
case class Trade(Date: String, Open: Double, High: Double, Low: Double, Close:Double, Volume:Double)

class SparkDatasetSpec extends FunSuite with Matchers with BeforeAndAfterAll {

  private lazy val sparkConf = new SparkConf().setAppName("spark_basic_dataset").setMaster("local[*]")
  private lazy val sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
  private lazy val sparkContext = sparkSession.sparkContext

  sparkContext.setLogLevel("INFO")
  lazy val url: URL = getClass.getResource("/goog.csv")

  test("Case 1: Show will show a minimal amount of data from the spark data set") {
    import sparkSession.implicits._
    val frame: DataFrame = sparkSession.read.csv(url.getFile)
    val dataset: Dataset[Int] = sparkSession.sparkContext.parallelize(1 to 1000).toDS()
  }

  //2.0 - Read an ORC or Hive, the default is DataSet, reduceByKey

  test("Case 2: Datasets can be created from a Seq") {
    import sparkSession.implicits._
    val dataset = sparkSession.createDataset(Seq("One", "Two", "Three"))
    dataset.foreach(s => println(s))
  }

  test("Case 3: Dataset can be explained before run") {
    sparkSession.range(1).filter(_ == 0).explain(true)
  }

  test("Case 4: Dataset can also be shown") {
    sparkSession.read.option("header", "true").csv(url.getFile).show()
  }

  test("Case 5: Dataset can also have a schema inferred") {
    sparkSession.read.option("header", "true").option("inferSchema", "true").csv(url.getFile).printSchema()
  }

  test("Case 6: Dataset can have a case class used in its place") {
    import sparkSession.implicits._
    val items: Dataset[Trade] = sparkSession.read.option("header", "true").option("inferSchema", "true").csv(url.getFile).as[Trade]
    items.filter(t=> t.Close > t.Open).foreach(t => println(t))
  }

  test("Case 7: Dataset can be expressed with a column") {
    import sparkSession.implicits._
    val items: Dataset[Row] = sparkSession.read.option("header", "true")
      .option("inferSchema", "true").csv(url.getFile)
    val date: ColumnName = $"Date"
    items.filter($"Date".endsWith("16")).sort(date.asc).show()
  }

  test("Case 8: Dataset can joined with a union") {
    import sparkSession.implicits._
    val items: Dataset[Row] = sparkSession.read.option("header", "true")
      .option("inferSchema", "true").csv(url.getFile)
    val date: ColumnName = $"Date"
    items.filter($"Date".endsWith("16")).sort(date.asc).show()
  }

  override protected def beforeAll(): Unit = {
    println("Setting up the spark context")
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    println("Press any key to terminate")
    StdIn.readLine()
    sparkSession.close()
    super.afterAll()
  }
}
