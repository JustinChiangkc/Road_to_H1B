val pivotEmployer = cdata2.groupBy("Fiscal Year","Employer").sum("TotalApproval").groupBy("Fiscal Year").pivot("Employer").sum("TotalApproval")
val pivotEmployer18 = data18.groupBy("Fiscal Year","Employer").sum("TotalApproval").groupBy("Fiscal Year").pivot("Employer").sum("TotalApproval")
val pivotDF = df.groupBy("Product","Country")
      .sum("Amount")
      .groupBy("Product")
      .pivot("Country")
      .sum("sum(Amount)")
pivotDF.show()

//Groupby emplyer, Fiscal Year, show top 10

//Groupby NAICS, Fiscal Year, show top 10

//Groupby State, Fiscal Year, show top 10

//TotalApproval & TotalDenial
val cdata2 = cdata.withColumn("TotalApproval", col("Initial Approvals") + col("Continuing Approvals")).withColumn("TotalDenial", col("Initial Denials") + col("Continuing Denials")).withColumn("TotalApply", col("TotalApproval") + col("TotalDenial"))

val Employer = cdata2.groupBy("Fiscal Year","Employer").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)

val State = cdata2.groupBy("Fiscal Year","State").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)

val NAICS = cdata2.groupBy("Fiscal Year","NAICS").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)

Employer.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/Employer")
State.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/State")
NAICS.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/NAICS")



///

from py4j.java_gateway import java_import
java_import(spark._jvm, 'org.apache.hadoop.fs.Path')

fs = spark._jvm.org.apache.hadoop.fs.FileSystem.get(spark._jsc.hadoopConfiguration())
file = fs.globStatus(sc._jvm.Path('mydata.csv-temp/part*'))[0].getPath().getName()
fs.rename(sc._jvm.Path('mydata.csv-temp/' + file), sc._jvm.Path('mydata.csv'))
fs.delete(sc._jvm.Path('mydata.csv-temp'), True)


cdata2.groupBy("Fiscal Year","Employer").sum("TotalApproval","TotalDenial","TotalApply")

val cdata2.groupBy("Fiscal Year","Employer").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("Employer"))

, col("sum(TotalApply)").desc