package spak.sql

import org.apache.spark.sql.functions.udf

class TestUDF {

  def main(args: Array[String]): Unit = {

    val len = udf{(str: String) => str.length }



  }

}
