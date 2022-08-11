package spak.sql.parse

import org.apache.spark.sql.SparkSession

/**
 * @author wangguochao
 * @date 2022/8/11
 */
object StrictParserTest {
  def main(args: Array[String]): Unit = {

    // 创建扩展点函数
    /**
     * 这里面有两个函数，extensionBuilder函数用于 SparkSession构建，
     */
//    type ParseBuilder = (SparkSession, ParserInterface) => ParserInterface
//    type ExtensionBuilder = SparkSessionExtensions => Unit
//    val parseBuilder: ParseBuilder = (_, parser) => new StrictParser(parser)
//    val extensionBuilder: ExtensionBuilder = {
//      e => e.injectParser(parseBuilder)
//    }

//    val conf = new SparkConf().setMaster("local").setAppName(this.getClass.getSimpleName).set("", )
    val spark = SparkSession.builder()
      .config("spark.master", "local")
      .getOrCreate()

//      .appName("Spark SQL basic example")
//      .config("spark.master", "local[*]")
//      .withExtensions(extensionBuilder)
//      .getOrCreate()

    val df = spark.read.json("C:\\Users\\wangguochao\\Desktop\\test_data\\people.json")
    df.toDF().write.saveAsTable("person")
    spark.sql("select * from person limit 3").show()
    spark.stop()
  }
}
