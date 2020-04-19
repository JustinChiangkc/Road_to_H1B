/// assumption : same company, similar job title, similar wage average ///////////////////////////
/// cuz we get acceptance rate by company, we now use avg salary per company, state and year ///// 


//import org.apache.spark.sql.{DataFrame, SparkSession}

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

///////////////////////// train data preprocessing ///////////////////////////////

// hdfs
// var salary = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load("/user/mt4050/project_data/h1b_salary_df_clean.csv")

// local
val salary_data = "file:///Users/anntsai5168/scala/project/project_data/h1b_salary_df_clean.csv"
val acceptance_data = "file:///Users/anntsai5168/scala/project/project_data/h1b_acceptance_rate_fake.csv"

// read in dataframe
val salary = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(salary_data).withColumnRenamed("year", "salary_year").withColumnRenamed("base salary", "base_salary")
val acceptance = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(acceptance_data)

//salary.show(5, true) // show(false) -> wont truncate the content
//acceptance.show(5)

// avg salary per company, state and year
val salary_avg = salary.groupBy($"state", $"salary_year").agg(bround(avg($"base_salary"), 2).as("avg_salary"))   // to 2 decimal
// salary_avg.show(5, true)

// join H1 B accepting rate data by company, state and year later
val salary_acceptance = salary_avg.join(acceptance, salary("salary_year") === acceptance("year"), "inner")
//salary_acceptance.show(true) 


// get column salary & rate, categorize by years [ 2012 ~2019, and ttl]

// 2019 [loop in the furture]
def filter_year(df: DataFrame , year: String): DataFrame = {
    return df.filter(df("year") === year)
}

val salary_acceptance_filter_year = filter_year(salary_acceptance, "2019")
//salary_acceptance_filter_year.show(5, true)

// make sure distinct year
salary_acceptance_filter_year.select("year", "salary_year").distinct().show(5, true)


// get train data
// rename to feature and label
// add company name later
val train_rawdata =  salary_acceptance_filter_year.select("avg_salary", "rate").withColumnRenamed("avg_salary", "feature").withColumnRenamed("rate", "label")
// train_rawdata.show(5, true)

// save train rawdata
// train_rawdata.coalesce(1).write.csv("train_rawdata.csv") // to several files
train_rawdata.coalesce(1).write.format("com.databricks.spark.csv").option("header", "true").save("train_rawdata")

//////////////////////////////// simple linear regression model //////////////////

import org.apache.spark.ml.regression.{LinearRegression, RandomForestRegressor}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{VectorAssembler, VectorIndexer}
import org.apache.spark.sql.SparkSession


// train function -> get slope

def lrWithSVMFormat(spark: SparkSession) : Double = {      // solve return type

  // load training data
  val rawdata = "file:///Users/anntsai5168/scala/project/train_rawdata"
  val train_rawdata = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(rawdata)
  // train_rawdata.show(5, true)

  // features n=must be array
  var assembler = new VectorAssembler().setInputCols(Array("feature")).setOutputCol("features")
  val train = assembler.transform(train_rawdata)

  // Split data into training (80%) and test (20%)
  val Array(training, test) = train.randomSplit(Array(0.8, 0.2), seed = 11L)
  training.show(5, true)

  // lr obj
  val lr = new LinearRegression()

  // traning
  val lrModel = lr.fit(training)

  // score
  val lrPredictions = lrModel.transform(test)
  val evaluator = new RegressionEvaluator()
  val score = evaluator.evaluate(lrPredictions)    // 4.299875284949258E-17 -> really small, will be updated after real data

  // Print the coefficients and intercept for linear regression
  val slope = lrModel.coefficients
  val intercept = lrModel.intercept
  println(s"Coefficients: ${slope} Intercept: ${intercept}")

  return slope(0)
}


// store slope for hyphothesis
val slope = lrWithSVMFormat(spark: SparkSession)
