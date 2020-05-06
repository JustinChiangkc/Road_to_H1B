val mydata_original = sc.textFile("h1b_salary.txt")
val mydata_clean = sc.textFile("h1b_salary_clean.txt")

///////////////////////////  ttl    /////////////////////////////

// the number of records - before clean
mydata_original.count() // res0: Long = 282542

// the number of records - after clean
mydata_clean.count() // res1: Long = 282541


// the number of records don't match
// reason : When I cleaned the data, I ignored the header.


///////////////////////////  salary /////////////////////////////

// salary max & min
val salary_list = mydata_clean.map{x=>x.split(',')}.map{x=>(x(2).toInt)}
println("Highest Salary:" + salary_list.max())  // Highest Salary:1350001
println("Lowest Salary:" + salary_list.min())  // Lowest Salary:923


////////////////////////////* hw 11 project code drop # 2 updated *////////////////////////////


///////////////////////////  employer ///////////////////////////

val salary_data = "/data/salary/h1b_salary_df_clean.csv"
val salary = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(salary_data).withColumnRenamed("year", "salary_year").withColumnRenamed("base salary", "base_salary").withColumnRenamed("case status", "status")


salary.createOrReplaceTempView("salary_view")
// spark.sql("SELECT * FROM salary_view").show(5)
/*
          employer|        job title|base_salary|   status|state|salary_year|month|
+------------------+-----------------+-----------+---------+-----+-----------+-----+
|           level 3|SOFTWARE ENGINEER|      96900|WITHDRAWN|   CO|       2012|    7|
|            amazon|SOFTWARE ENGINEER|     115000|WITHDRAWN|   WA|       2012|    8|
|stmicroelectronics|SOFTWARE ENGINEER|     110820|WITHDRAWN|   CA|       2012|    7|
|     contentactive|SOFTWARE ENGINEER|      51979|WITHDRAWN|   TX|       2012|    4|
|             xerox|SOFTWARE ENGINEER|      74196|WITHDRAWN|   NY|       2012|    4|
*/
spark.sql("SELECT DISTINCT(employer) FROM salary_view").show(5)
spark.sql("SELECT COUNT(DISTINCT(employer)) FROM salary_view").show() // bf : 21928 , af : 19719


/////////////////////////// case status /////////////////////////

import org.apache.spark.SparkConf


val salary_data = "/data/salary/h1b_salary_df_clean.csv"
val salary = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(salary_data).withColumnRenamed("year", "salary_year").withColumnRenamed("base salary", "base_salary").withColumnRenamed("case status", "status")


salary.createOrReplaceTempView("salary_view")

// status compostion 
val status = spark.sql("SELECT status, COUNT(*) as records FROM salary_view GROUP BY status")
status.show()
/*
|   status|records|
+---------+-------+
|CERTIFIED| 268885|
|WITHDRAWN|  10695|
|   DENIED|   2961|
*/

val ttl = spark.sql("SELECT COUNT(*) as ttl FROM salary_view")
ttl.show() // 282541


// cross join
var sparkConf: SparkConf = null
sparkConf = new SparkConf().set("spark.sql.crossJoin.enabled", "true")

status.createOrReplaceTempView("status_view")
ttl.createOrReplaceTempView("ttl_view")

val compostion = spark.sqlContext.sql("select * from status_view CROSS JOIN ttl_view")
compostion.createOrReplaceTempView("compostion_view")


// result
spark.sql("SELECT *, round(records/ttl, 2) as rate FROM compostion_view").show()

/*
   status|records|   ttl|rate|
+---------+-------+------+----+
|CERTIFIED| 268885|282541|0.95|
|WITHDRAWN|  10695|282541|0.04|
|   DENIED|   2961|282541|0.01|
*/