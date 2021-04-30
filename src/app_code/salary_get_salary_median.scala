import org.apache.spark.sql.DataFrame


///////////////////////////  calculate salary median /////////////////////////// 

val salary_data = "/data/salary/h1b_salary_df_clean.csv"
val salary_raw = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(salary_data).withColumnRenamed("year", "salary_year").withColumnRenamed("base salary", "base_salary")
// employer name escape, ex. zeng's fabric -> zengs fabric
//   employer|        job title|base_salary|case status|state|salary_year|month|   parsed_employer|
val salary = salary_raw.drop("job title", "case status", "month")


def getMedian(salary: DataFrame) = {

    // create view
    salary.createOrReplaceTempView("salary_view")
    spark.sql("SELECT * FROM salary_view").show(5)

    // create output dataframe
    val output = spark.sql("select salary_year as year, state, employer, percentile_approx(base_salary, 0.5) as salary_median from salary_view group by salary_year, state, employer")
    //output.show(5)

    output.createOrReplaceTempView("output_view")
    //spark.sql("SELECT DISTINCT(year) FROM output_view").show()

    // save as csv
    output.coalesce(1).write.format("com.databricks.spark.csv").option("header", "true").save("/data/salary/h1b_median_salary.csv")

}


getMedian(salary)


/*
|year|state|       employer|salary_median|
+----+-----+---------------+-------------+
|2014|   CA|          banjo|       130000|
|2014|   CA| maintenancenet|        83000|
|2014|   CA|     mobileiron|        98488|
|2014|   CA|        populus|        93340|
|2014|   CA|tera operations|       105898|
......
*/
