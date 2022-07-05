### 1. 为Spark SQL 添加一条自定义命令

- SHOW VERSION；
- 显示当前 Spark 版本和 Java 版本

#### 1.1 下载 spark 源码

链接：https://archive.apache.org/dist/spark/spark-3.2.0/



#### 1.2 编辑源码

##### 1.2.1 sql/catalyst/src/main/antlr4/org/apache/spark/sql/catalyst/parser/SqlBase.g4

内容如下：

```shell
#修改四处
statement
| SHOW VERSION                                                     #showVersion

ansiNonReserved
| VERSION

nonReserved
| VERSION

//--SPARK-KEYWORD-LIST-START
VERSION: 'VERSION' | 'V';
```

提交结果如下：

<center>
<img src=".\编辑SQLBase.webp">
</br>
代码提交结果
</center>



##### 1.2.2 Maven 编译 antlr

<center>
<img src=".\编译antlr.png">
</br>
编译antlr
</center>



##### 1.2.3 修改 SqlParsqlParser.scala

文件位于：sql/core/src/main/scala/org/apache/spark/sql/execution/SparkSqlParser.scala

1. 增加 visitShowVersion方法

   ```scala
   override def visitShowVersion(ctx: ShowVersionContext): LogicalPlan = withOrigin(ctx) {
       ShowVersionCommand()
   }
   ```

   

2. 实现ShowVersionCommand类

   添加文件：sql/core/src/main/scala/org/apache/spark/sql/execution/command/ShowVersionCommand.scala

   ```scala
    /*
    * Licensed to the Apache Software Foundation (ASF) under one or more
    * contributor license agreements.  See the NOTICE file distributed with
    * this work for additional information regarding copyright ownership.
    * The ASF licenses this file to You under the Apache License, Version 2.0
    * (the "License"); you may not use this file except in compliance with
    * the License.  You may obtain a copy of the License at
    *
    *    http://www.apache.org/licenses/LICENSE-2.0
    *
    * Unless required by applicable law or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS,
    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    * See the License for the specific language governing permissions and
    * limitations under the License.
    */
   
   package org.apache.spark.sql.execution.command
   
   import org.apache.spark.sql.{Row, SparkSession}
   import org.apache.spark.sql.catalyst.expressions.{Attribute, AttributeReference}
   import org.apache.spark.sql.types.StringType
   
   
   case class ShowVersionCommand() extends LeafRunnableCommand {
   
     override val output: Seq[Attribute] =
       Seq(AttributeReference("version", StringType)())
   
     override def run(sparkSession: SparkSession): Seq[Row] = {
       val sparkVersion = sparkSession.version
       val javaVersion = System.getProperty("java.version")
       val scalaVersion = scala.util.Properties.releaseVersion
       val output = "Spark Version: %s, Java Version: %s, Scala Version: %s"
         .format(sparkVersion, javaVersion, scalaVersion.getOrElse(""))
       Seq(Row(output))
     }
   }
   ```

#### 1.3 Maven 编译源码

在 spark目录，执行以下命令

```shell
build/mvn clean package -DskipTests -Phive -Phive-thriftserver
```

参考链接：https://spark.apache.org/docs/latest/building-spark.html



#### 1.4 执行命令

linux执行：

```shell
./bin/spark-sql
```

windows执行：

```shell
./bin/spark-sql.cmd
```



执行结果如下：

<center>
<img src=".\执行结果.png">
</br>
执行结果
</center>
#### 链接参考：

- https://xie.infoq.cn/article/4512f8861d882a1da512084d9
- https://blog.csdn.net/nzbing/article/details/124653021



### 2. 构建 SQL 满足如下要求

通过 set spark.sql.planChangeLog.level=WARN; 查看

1.  构建一条SQL，同时 apply 下面三条优化规则：  

   - CombineFilters

   - CollapseProject
   - BooleanSimlification

2. 构建一条SQL，同时 apply 下面五条优化规则：

   - ConstantFloding
   - PushDownPredicates
   - ReplaceDistinctWithAggregate
   - ReplaceExceptWithAntJoin
   - FoldablePropagation

#### 准备工作

数据源：finances-small.json

```json
{"ID":1,"Account":{"Number":"123-ABC-789","FirstName":"Jay","LastName":"Smith"},"Date":"1/1/2015","Amount":1.23,"Description":"Drug Store"},
{"ID":2,"Account":{"Number":"456-DEF-456","FirstName":"Sally","LastName":"Fuller"},"Date":"1/3/2015","Amount":200.00,"Description":"Electronics"},
{"ID":3,"Account":{"Number":"333-XYZ-999","FirstName":"Brad","LastName":"Turner"},"Date":"1/4/2015","Amount":106.00,"Description":"Gas},
{"ID":4,"Account":{"Number":"987-CBA-321","FirstName":"Justin","LastName":"Pihony"},"Date":"1/4/2015","Amount":0.00,"Description":"Drug Store"}
...
```



导入数据：

```shell
1、在HDFS 上创建多级目录
hdfs dfs -mkdir -p /home/student5/wangguochao/datasource

2、上传数据
hdfs dfs -put /home/student5/wangguochao/04_spark/finances-small.json /home/student5/wangguochao/datasource
```



创建视图：

```sql
spark-sql> CREATE TEMPORARY TABLE finance USING org.apache.spark.sql.json  OPTIONS (path '/home/student5/wangguochao/datasource/finances-small.json');
22/07/05 11:01:32 WARN [main] SparkSqlAstBuilder: CREATE TEMPORARY TABLE ... USING ... is deprecated, please use CREATE TEMPORARY VIEW ... USING ... instead
Time taken: 2.987 seconds
```





#### 2.1 题1，应用三条优化规则

```sql
CREATE TABLE t1(a1 INT, a2 INT) USING parquet;

SELECT a11, (a2+1) AS a21
FROM (
	SELECT (a1 +1) AS a11, a2 FROM t1 WHERE a1>10
) WHERE a11>1 AND 1=1;
```



#### 2.2 题2，应用五条优化规则

```sql
CREATE TABLE t1(a1 INT, a2 INT) USING parquet;
CREATE TABLE t2(b1 INT, b2 INT) USING parquet;

SELECT DISTINCT a1, a2, 'custom' a3
FROM (
	SELECT * FROM t1 WHERE a2=10 AND 1=1
) WHERE a1>5 AND 1=1
EXCEPT SELECT b1, b2, 1.0 b3 FROM t2 WHERE b2=10;
```



### 3. 实现自定义优化规则（静默规则）

见代码：package spak.sql



### 参考连接

- https://xie.infoq.cn/article/4512f8861d882a1da512084d9

- https://xie.infoq.cn/article/6f8ca37044dce47facfee7ae0