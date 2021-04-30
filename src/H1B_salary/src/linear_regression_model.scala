/// assumption : same company, similar job title, similar wage average ///////////////////////////
/// cuz we get acceptance rate by company, we now use avg salary per company, state and year ///// 

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.ml.regression.LinearRegressionSummary

///////////////////////// train data preprocessing /////////////////////////////// hw 11 project code drop # 2 updated

// read in salary data
// hdfs
// val salary_data = "/user/mt4050/h1b_median_salary.csv" 

val salary_data = "/user/mt4050/project_data/h1b_median_salary.csv"
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
/*

salary.createOrReplaceTempView("salary_view")
val acceptance_data = spark.sql("select year as rate_year, state as rate_state, employer as rate_employer, round(rand(), 2) as rate from salary_view")
acceptance_data.show(5)

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
val acceptance_data = "/user/mt4050/project_data/h1b_acceptance_rate_clean"
val acceptance = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(acceptance_data)
acceptance.createOrReplaceTempView("acceptance_view")
val acceptance = spark.sql("select FiscalYear as rate_year, State as rate_state, Employer as rate_employer, ApprovalRate as rate from acceptance_view")
acceptance.show(5)
/*
+---------+----------+--------------------+------------------+
|rate_year|rate_state|       rate_employer|              rate|
+---------+----------+--------------------+------------------+
|     2012|        NJ|cognizant tech so...|0.9382590060210225|
|     2012|        MD|    tata consultancy|0.9622737055323458|
|     2014|        MD|    tata consultancy|0.9345468146116445|
|     2013|        MD|    tata consultancy|0.9614923584057536|
|     2013|        TX|             infosys|0.9332361516034985|
+---------+----------+--------------------+------------------+
*/

// join H1 B accepting rate data by company, state and year
val salary_acceptance = salary.join(acceptance).where(salary("year") === acceptance("rate_year") && salary("state") === acceptance("rate_state") && salary("employer") === acceptance("rate_employer"))
salary_acceptance.createOrReplaceTempView("salary_acceptance_view")
salary_acceptance.show(true) 
/*
year|state|          employer|salary_median|rate_year|rate_state|     rate_employer|              rate|
+----+-----+------------------+-------------+---------+----------+------------------+------------------+
|2014|   MD|  tata consultancy|        66900|     2014|        MD|  tata consultancy|0.9345468146116445|
|2015|   IL|       accenture l|        88139|     2015|        IL|       accenture l|0.9562308335656537|
|2019|   WA|         amazoncom|       135000|     2019|        WA|         amazoncom|0.9612575420768498|
|2019|   CA|            google|       135000|     2019|        CA|            google|0.9627240143369176|
|2018|   WA|amazon fulfillment|       127400|     2018|        WA|amazon fulfillment| 0.990495867768595|
|2017|   WA|            amazon|       128000|     2017|        WA|            amazon|0.9899255365746824|
|2017|   NJ|     tech mahindra|        94828|     2017|        NJ|     tech mahindra|0.9151414309484193|
|2016|   IL|       accenture l|        91541|     2016|        IL|       accenture l|0.9788247750132345|
|2015|   NC| ibm india private|        68016|     2015|        NC| ibm india private|0.9710884353741497|
|2019|   WA|             micro|       134000|     2019|        WA|             micro|0.9399117971334069|
|2019|   CA|          facebook|       160000|     2019|        CA|          facebook|0.9745060548119822|
|2017|   WA|             micro|       120776|     2017|        WA|             micro|0.9878950907868191|
|2014|   NC| ibm india private|        67371|     2014|        NC| ibm india private|0.9635193133047211|
*/


// rm outlier
val outlier = spark.sql("select * from salary_acceptance_view where rate = 0")
outlier.count  //res31: Long = 1275
val salary_acceptance = spark.sql("select * from salary_acceptance_view where rate != 0")

// check joining
salary.count
//res24: Long = 79543
acceptance.count
//res24: Long = 227329
salary_acceptance.count
//res23: Long = 18615


salary_acceptance.repartition(1).write.mode ("overwrite").format("com.databricks.spark.csv").option("header", "true").save("/user/mt4050/project_data/salary_acceptance")


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
        val count = salary_acceptance_filter_year.count

        // get train data - rename to feature and label
        //val train_rawdata =  salary_acceptance_filter_year.select("salary_median", "rate").withColumnRenamed("salary_median", "feature").withColumnRenamed("rate", "label")
        val train_rawdata =  salary_acceptance_filter_year.select("salary_median", "rate")
        // train_rawdata.show(5, true)
        /*
        +-------+------------------+
        |feature|             label|
        +-------+------------------+
        | 135000|0.9612575420768498|
        | 135000|0.9627240143369176|
        | 134000|0.9399117971334069|
        | 160000|0.9745060548119822|
        |  83699|0.7419150285351934|
        */
        // train_rawdata.printSchema()
        /*
        root
        |-- feature: integer (nullable = true)
        |-- label: double (nullable = true)
        */

        // save train rawdata
        // train_rawdata.coalesce(1).write.format("com.databricks.spark.csv").option("header", "true").mode("overwrite").save("/user/mt4050/project_data/train_rawdata")

        // load training data
        // val rawdata = "file:///Users/anntsai5168/scala/project/train_rawdata"
        // val train_rawdata = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(rawdata)
        // train_rawdata.show(5, true)

        // features must be array
        val assembler = new VectorAssembler().setInputCols(Array("salary_median")).setOutputCol("features")
        val training = assembler.transform(train_rawdata)
        val linearRegression2 = new LinearRegression().setMaxIter(10).setLabelCol("rate") 
        /*
        training.show()
        +-------------+------------------+----------+
        |salary_median|              rate|  features|
        +-------------+------------------+----------+
        |       135000|0.9612575420768498|[135000.0]|
        |       135000|0.9627240143369176|[135000.0]|
        |       134000|0.9399117971334069|[134000.0]|
        |       160000|0.9745060548119822|[160000.0]|
        |        83699|0.7419150285351934| [83699.0]|
        |       164674|0.9760479041916168|[164674.0]|
        |        99736|0.6274247491638796| [99736.0]|
        */

        // Split data into training (80%) and test (20%)
        //val Array(training, test) = train.randomSplit(Array(0.8, 0.2), seed = 11L)
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

        // traning
        val lrModel = linearRegression2.fit(training)
        //println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}") 
        val (coefficient, intercept) = (lrModel.coefficients(0), lrModel.intercept) 

        // score
        /*
        val lrPredictions = lrModel.transform(test)
        val evaluator = new RegressionEvaluator()
        val score = evaluator.evaluate(lrPredictions)

        println(s"score: $score")
        */

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
        val MSE = summary.meanSquaredError
        val r_square =  summary.r2   // between 0 ~ 1, coefficient of determination

        //E(MSE) = σ^2
        //Var(i) = σ^2

        // avg median salary : x¯
        val avg_median_salary = train_rawdata.agg(round(avg(col("salary_median")),2)).collect.map(_.toSeq).flatten
        println(s"count: $count, avg of median salary: ${avg_median_salary(0)} -> Yi = ${coefficient} *(Xi) + ϵi")

        ///////////////////////////  hypothesis testing /////////////////////////////////  hw 11 project code drop # 2 updated

        /*
        Objective -> test the Credibility of linear equation [salary, acceptance rate]
        Method -> test the linear equation we get is siginificant or not
        α -> 0.05
        */
        
        // p-value
        val pvalue = summary.pValues(0)

        // judgement
        var isSignificant: Boolean = false;
        if (pvalue < 0.05) {
            isSignificant = true;
        }
        else {
            isSignificant = false;
        }
        println(s"p value: ${pvalue} , isSignificant : ${isSignificant}")

        ///////////////////////////  sensitivity analysis /////////////////////////////////

        // delta_x = 10 %
        println(s"Sensitivity Analysis : delta_x = 10 % -> delta_y = ${coefficient * 0.1} %\n")   // ignore ϵi

    }

}


// execution
lrWithSVMFormat(salary_acceptance, year_array)
