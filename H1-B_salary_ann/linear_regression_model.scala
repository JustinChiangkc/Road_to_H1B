/// assumption : same company, similar job title, similar wage average ///////////////////////////
/// cuz we get acceptance rate by company, we now use avg salary per company, state and year ///// 

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.ml.regression.LinearRegressionSummary

///////////////////////// train data preprocessing /////////////////////////////// hw 11 project code drop # 2 updated

// read in salary data
// hdfs
// val salary_data = "/user/mt4050/h1b_median_salary.csv" 

val salary_data = "file:///Users/anntsai5168/scala/project/project_data/h1b_median_salary.csv"
val salary = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(salary_data)
salary.show(5)
/*
+----+-----+---------------+-------------+
|year|state|       employer|salary_median|
+----+-----+---------------+-------------+
|2014|   CA|          banjo|       130000|
|2014|   CA| maintenancenet|        83000|
|2014|   CA|     mobileiron|        98488|
|2014|   CA|        populus|        93340|
|2014|   CA|tera operations|       105898|
*/


// create fake acceptance data
salary.createOrReplaceTempView("salary_view")
val acceptance_data = spark.sql("select year as rate_year, state as rate_state, employer as rate_employer, round(rand(), 2) as rate from salary_view")
acceptance_data.show(5)
/*
|rate_year|rate_state|  rate_employer|rate|
+---------+----------+---------------+----+
|     2014|        CA|          banjo|0.27|
|     2014|        CA| maintenancenet|0.33|
|     2014|        CA|     mobileiron|0.71|
|     2014|        CA|        populus|0.25|
|     2014|        CA|tera operations|0.49|
+----+-----+------------------+----+
*/

// read in real acceptance_data
//val acceptance_data = "file:///Users/anntsai5168/scala/project/project_data/h1b_acceptance_rate_fake.csv"
//val acceptance = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(acceptance_data)
val acceptance = acceptance_data;


// join H1 B accepting rate data by company, state and year
val salary_acceptance = salary.join(acceptance).where(salary("year") === acceptance("rate_year") && salary("state") === acceptance("rate_state") && salary("employer") === acceptance("rate_employer"))
acceptance.createOrReplaceTempView("acceptance_view")
salary_acceptance.createOrReplaceTempView("salary_acceptance_view")
//salary_acceptance.show(true) 
/*
+----+-----+--------------------+-------------+---------+----------+--------------------+----+
|year|state|            employer|salary_median|rate_year|rate_state|       rate_employer|rate|
+----+-----+--------------------+-------------+---------+----------+--------------------+----+
|2014|   CA|               banjo|       130000|     2014|        CA|               banjo|0.27|
|2014|   CA|      maintenancenet|        83000|     2014|        CA|      maintenancenet|0.33|
|2014|   CA|          mobileiron|        98488|     2014|        CA|          mobileiron|0.71|
|2014|   CA|             populus|        93340|     2014|        CA|             populus|0.25|
|2014|   CA|     tera operations|       105898|     2014|        CA|     tera operations|0.49|
|2014|   CO|               ascii|        61760|     2014|        CO|               ascii|0.41|
|2014|   FL|   sritech soultions|        63000|     2014|        FL|   sritech soultions|0.46|
|2014|   GA|               birla|        57949|     2014|        GA|               birla|0.17|
|2014|   GA|              primus|        75000|     2014|        GA|              primus|0.37|
|2014|   GA|    recruiting minds|        75000|     2014|        GA|    recruiting minds|0.11|
*/


// check joining
spark.sql("SELECT count(*) FROM acceptance_view").show()    // 79543
spark.sql("SELECT count(*) FROM salary_view WHERE year = 2019").show() // 17029
spark.sql("SELECT count(*) FROM salary_view").show() // 79543
spark.sql("SELECT count(*) FROM salary_acceptance_view").show()    // 79351
spark.sql("SELECT count(*) FROM salary_acceptance_view WHERE (year = '2014' and state = 'CA' and employer = 'banjo')").show() // 1


// get all years
val year_array = spark.sql("SELECT DISTINCT(year) FROM salary_acceptance_view ORDER BY year").collect.map(_.toSeq).flatten

//////////////////////////////// simple linear regression model //////////////////  hw 11 project code drop # 2 updated

import org.apache.spark.ml.regression.{LinearRegression, RandomForestRegressor, LinearRegressionSummary}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{VectorAssembler, VectorIndexer}
import org.apache.spark.sql.SparkSession


// train function

def filter_year(df: DataFrame , year: Any): DataFrame = {
    return df.filter(df("year") === year)
}

def lrWithSVMFormat(salary_acceptance: DataFrame, year_array: Array[Any]) = {

    // filter year
    for (year <- year_array) {

        println(s"====================== year: $year ======================")
        
        val salary_acceptance_filter_year = filter_year(salary_acceptance , year)
        //salary_acceptance_filter_year.show(5, true)

        // get train data - rename to feature and label
        val train_rawdata =  salary_acceptance_filter_year.select("salary_median", "rate").withColumnRenamed("salary_median", "feature").withColumnRenamed("rate", "label")
        // train_rawdata.show(5, true)
        /*
        +-------+-----+
        |feature|label|
        +-------+-----+
        |  75276| 0.72|
        | 115000| 0.72|
        | 113381| 0.72|
        |  81162| 0.72|
        |  79000| 0.72|
        */
        // train_rawdata.printSchema()
        /*
        root
        |-- feature: integer (nullable = true)
        |-- label: double (nullable = true)
        */

        // save train rawdata
        // train_rawdata.coalesce(1).write.format("com.databricks.spark.csv").option("header", "true").mode("overwrite").save("file:///Users/anntsai5168/scala/project/project_data/train_rawdata")

        // load training data
        // val rawdata = "file:///Users/anntsai5168/scala/project/train_rawdata"
        // val train_rawdata = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(rawdata)
        // train_rawdata.show(5, true)

        // features must be array
        var assembler = new VectorAssembler().setInputCols(Array("feature")).setOutputCol("features")
   
        val train = assembler.transform(train_rawdata)

        // Split data into training (80%) and test (20%)
        val Array(training, test) = train.randomSplit(Array(0.8, 0.2), seed = 11L)
        //training.show(5, true)
        /*
        training.show(5, true)
        +-------+-----+---------+
        |feature|label| features|
        +-------+-----+---------+
        |   1483| 0.07| [1483.0]|
        |   2885| 0.98| [2885.0]|
        |   3750|  0.1| [3750.0]|
        |   9000| 0.39| [9000.0]|
        |  10000| 0.52|[10000.0]|
        +-------+-----+---------+
        */

        // lr obj
        val lr = new LinearRegression()

        // traning
        val lrModel = lr.fit(training)

        // score
        val lrPredictions = lrModel.transform(test)
        val evaluator = new RegressionEvaluator()
        val score = evaluator.evaluate(lrPredictions)    // 4.299875284949258E-17 -> really small, will be updated after real data

        // Linear equation : Print the coefficients, intercept  and MSE for linear regression
        /*
        linear equation -> Yi= β(xi)+ϵi
            Yi: value of the response variable in the ith trial [H1-B acceptance rate]
            Xi: value of the response variable in the ith trial [median of salary [per company and state]]
            bar x: the mean of xi
            β : coefficient
        */
        val summary = lrModel.summary
        
        // The intercept (often labeled the constant) is the expected mean value of Y when all X=0.
        // if X never = 0, there is no interest in the intercept. It doesn’t tell you anything about the relationship between X and Y.
        // CUZ salary != 0, so we dont care intercept in this case
        val (coefficient, intercept) = (summary.tValues(0), summary.tValues(1))
        val MSE = summary.meanSquaredError
        val r_square =  summary.r2   // between 0 ~ 1, coefficient of determination

        //E(MSE) = σ^2
        //Var(i) = σ^2

        // avg median salary : x¯
        val avg_median_salary = train_rawdata.agg(round(avg(col("feature")),2)).collect.map(_.toSeq).flatten
        println(s"year: $year, avg of median salary: ${avg_median_salary(0)} -> Yi = ${coefficient} *(Xi) + ϵi")

        ///////////////////////////  hypothesis testing /////////////////////////////////  hw 11 project code drop # 2 updated

        /*
        Objective -> test the Credibility of linear equation [salary, acceptance rate]
        Method -> test the slope of linear equation is siginificant or not

        a (1−α)100% confidence interval for β [two-tailed t test]:
            null hypothesis -> H0 : β=β0  [β0 : the slope we got]
            alternative hypotheses -> HA : β≠β0, HA:β<β0, HA:β>β0  
            test statistic -> t = ^β−β0 / [(MSE/∑(xi−x)^2)**2]
            α -> 0.05
        */
        
        // p-value
        val pvalue = summary.pValues(0)

        // judgement
        if (pvalue < 0.05) {
            val isSignificant: Boolean = true;
        }
        else {
            val isSignificant: Boolean = false;
        }

        println(s"p value: ${pvalue} , isSignificant : ${isSignificant}")

        ///////////////////////////  sensitivity analysis /////////////////////////////////

        // delta_x = 10 %
        println(s"Sensitivity Analysis : delta_x = 10 % -> delta_y = ${coefficient * 0.1}")   // ignore ϵi
        println("==========================================================")

    }

}


// execution
lrWithSVMFormat(salary_acceptance, year_array)



