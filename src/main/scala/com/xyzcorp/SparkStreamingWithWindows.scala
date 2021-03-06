package com.xyzcorp

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreamingWithWindows extends App {

  //
  //run with nc -lk 9090

  val conf: SparkConf = new SparkConf().setAppName("streaming_1").setMaster("local[*]")
  val streamingContext = new StreamingContext(conf, Seconds(1)) //Seconds comes from streaming

  streamingContext.sparkContext.setLogLevel("INFO")

  val lines = streamingContext.socketTextStream("localhost", 10150)

  //produce information over the last 30 seconds of data, every 10 seconds
  val windowedStream = lines.window(Seconds(30), Seconds(10))
  windowedStream.print()

  streamingContext.start()
  streamingContext.awaitTermination()
}
