//load in data
val dir = "data/h1b_cleaned_data"
val cdata = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv(dir)

//AR_Employer_State
val AR_Year_Employer_State = cdata.filter(col("Fiscal Year")>2011).groupBy("Fiscal Year","State","Employer").sum("Initial Approvals","Initial Denials").withColumn("ApprovalRate", col("sum(Initial Approvals)")/(col("sum(Initial Approvals)")+col("sum(Initial Denials)"))).sort(col("sum(Initial Approvals)").desc).na.drop.filter(col("ApprovalRate") > 0)
AR_Year_Employer_State.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("data/AR_Year_Employer_State")
