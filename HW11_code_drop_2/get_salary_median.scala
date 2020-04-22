///////////////////////////  calculate salary median /////////////////////////// 

val salary_data = "file:///Users/anntsai5168/scala/project/project_data/h1b_salary_df_clean.csv"
val salary = spark.sqlContext.read.format("csv").option("header", "true").option("inferSchema", "true").load(salary_data).withColumnRenamed("year", "salary_year").withColumnRenamed("base salary", "base_salary")

// collect all distinct years & states   [add company in the future]
salary.createOrReplaceTempView("salary_view")
spark.sql("SELECT * FROM salary_view").show(5)

val year_array = spark.sql("SELECT DISTINCT(salary_year) FROM salary_view").collect.map(_.toSeq).flatten
val state_array = spark.sql("SELECT DISTINCT(state) FROM salary_view").collect.map(_.toSeq).flatten


def getMedian(year_array: Array[Any], state_array: Array[Any]) = {

    salary.createOrReplaceTempView("salary_view")
    spark.sql("SELECT * FROM salary_view").show(5)
    
    for (year <- year_array; state <- state_array) {  
        val salary_year = year.toString
        val median = spark.sql(s"""select percentile_approx(base_salary, 0.5) from salary_view where (salary_year ='$salary_year' and state = '$state')""").first()(0)
        println(s"salary_year: ${salary_year} state: ${state} salary_median: ${median}")
    }
}

getMedian(year_array, state_array)


/*
salary_year: 2018 state: AZ salary_median: 81266
salary_year: 2018 state: SC salary_median: 74500
salary_year: 2018 state: LA salary_median: 66000
salary_year: 2018 state: MN salary_median: 80000
salary_year: 2018 state: NJ salary_median: 93000
salary_year: 2018 state: DC salary_median: 93000
salary_year: 2018 state: OR salary_median: 86196
salary_year: 2018 state: VA salary_median: 92019
......
*/
