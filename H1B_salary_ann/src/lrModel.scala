import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.ml.regression.LinearRegressionSummary

import org.apache.spark.ml.regression.{LinearRegression, RandomForestRegressor, LinearRegressionSummary}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{VectorAssembler, VectorIndexer}
import org.apache.spark.sql.SparkSession


object lrModel {

    def main(args:Array[String]){  

        val salary_data = "/user/mt4050/project_data/h1b_median_salary.csv"
        val salary = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(salary_data)

        // read in real acceptance_data
        val acceptance_data = "/user/mt4050/project_data/h1b_acceptance_rate_clean"
        val acceptance = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(acceptance_data)
        acceptance.createOrReplaceTempView("acceptance_view")
        val acceptance = spark.sql("select FiscalYear as rate_year, State as rate_state, Employer as rate_employer, ApprovalRate as rate from acceptance_view")

        // join H1 B accepting rate data by company, state and year
        val salary_acceptance = salary.join(acceptance).where(salary("year") === acceptance("rate_year") && salary("state") === acceptance("rate_state") && salary("employer") === acceptance("rate_employer"))
        salary_acceptance.createOrReplaceTempView("salary_acceptance_view")

        // rm outlier
        val salary_acceptance = spark.sql("select * from salary_acceptance_view where rate != 0")

        // save as csv
        salary_acceptance.repartition(1).write.mode ("overwrite").format("com.databricks.spark.csv").option("header", "true").save("/user/mt4050/project_data/salary_acceptance")

        // get all years
        val year_array = spark.sql("SELECT DISTINCT(year) FROM salary_acceptance_view ORDER BY year").collect.map(_.toSeq).flatten

        lrWithSVMFormat(salary_acceptance, year_array)
    }  


    def filter_year(df: DataFrame , year: Any): DataFrame = {
        return df.filter(df("year") === year)
    }


    def lrWithSVMFormat(salary_acceptance: DataFrame, year_array: Array[Any]) = {

        // filter year
        for (year <- year_array) {

            println(s"====================== year: $year ======================")
            
            val salary_acceptance_filter_year = filter_year(salary_acceptance , year)
            val count = salary_acceptance_filter_year.count

            val train_rawdata =  salary_acceptance_filter_year.select("salary_median", "rate")
            
            // features must be array
            val assembler = new VectorAssembler().setInputCols(Array("salary_median")).setOutputCol("features")
            val training = assembler.transform(train_rawdata)
            val linearRegression2 = new LinearRegression().setMaxIter(10).setLabelCol("rate") 
            
            // traning
            val lrModel = linearRegression2.fit(training)
            //println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}") 
            val (coefficient, intercept) = (lrModel.coefficients(0), lrModel.intercept) 

            ///////////////////////////  Linear equation   /////////////////////////////////

            val summary = lrModel.summary
            
            // avg median salary : x¯
            val avg_median_salary = train_rawdata.agg(round(avg(col("salary_median")),2)).collect.map(_.toSeq).flatten
            println(s"count: $count, avg of median salary: ${avg_median_salary(0)} -> Yi = ${coefficient} *(Xi) + ϵi")

            ///////////////////////////  hypothesis testing /////////////////////////////////
            
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

}
