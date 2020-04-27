import org.apache.spark.sql.DataFrame

val dir = "final_project/data/cleaned_data"
val cdata = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv(dir)
//Groupby emplyer, Fiscal Year, show top 10

//Groupby NAICS, Fiscal Year, show top 10

//Groupby State, Fiscal Year, show top 10

//TotalApproval & TotalDenial
val cdata2 = cdata.withColumn("TotalApproval", col("Initial Approvals") + col("Continuing Approvals")).withColumn("TotalDenial", col("Initial Denials") + col("Continuing Denials")).withColumn("TotalApply", col("TotalApproval") + col("TotalDenial"))

//Groupby emplyer, Fiscal Year
// val Employer = cdata2.groupBy("Fiscal Year","Employer").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)
// //Groupby NAICS, Fiscal Year
// val State = cdata2.groupBy("Fiscal Year","State").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)
// //Groupby State, Fiscal Year
// val NAICS = cdata2.groupBy("Fiscal Year","NAICS").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)


///
// val Employer = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/Employer")
// val State = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/State")
// val NAICS = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/NAICS")

val AR_Employer = cdata2.filter(col("Fiscal Year")>2011).groupBy("Employer").sum("TotalApproval","TotalDenial","TotalApply").withColumn("ApprovalRate", col("sum(TotalApproval)")/col("sum(TotalApply)"))sort(col("sum(TotalApply)").desc)
val AR_State = cdata2.filter(col("Fiscal Year")>2011).groupBy("State").sum("TotalApproval","TotalDenial","TotalApply").withColumn("ApprovalRate", col("sum(TotalApproval)")/col("sum(TotalApply)"))sort(col("sum(TotalApply)").desc)
val AR_NAICS = cdata2.filter(col("Fiscal Year")>2011).groupBy("NAICS").sum("TotalApproval","TotalDenial","TotalApply").withColumn("ApprovalRate", col("sum(TotalApproval)")/col("sum(TotalApply)"))sort(col("sum(TotalApply)").desc)


//Save
AR_Employer.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/AR_Employer2")
AR_State.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/AR_State2")
AR_NAICS.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/AR_NAICS2")

//Load 
val AR_Employer = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/AR_Employer2")
val AR_State = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/AR_State2")
val AR_NAICS = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/AR_NAICS2")

def get_trend_Employer(Employer:String, df:DataFrame): DataFrame = {
	val trend:DataFrame = df.filter(col("Employer") === Employer)
	.groupBy("Fiscal Year").sum("Initial Approvals","Initial Denials")
	.withColumn("ApprovalRate", col("sum(Initial Approvals)")/(col("sum(Initial Approvals)")+col("sum(Initial Denials)")))
	.sort(col("Fiscal Year").desc) 
	return trend
}


//HW11 update
def get_trend_State(State:String, df:DataFrame): DataFrame = {
	val trend:DataFrame = df.filter(col("State") === State)
	.groupBy("Fiscal Year").sum("Initial Approvals","Initial Denials")
	.withColumn("ApprovalRate", col("sum(Initial Approvals)")/(col("sum(Initial Approvals)")+col("sum(Initial Denials)")))
	.sort(col("Fiscal Year").desc) 
	return trend
}

def get_trend_State(State:String, df:DataFrame): DataFrame = {
	val trend:DataFrame = df.filter(col("State") === State)
	.groupBy("Fiscal Year").sum("Initial Approvals","Initial Denials")
	.withColumn("ApprovalRate", col("sum(Initial Approvals)")/(col("sum(Initial Approvals)")+col("sum(Initial Denials)")))
	.sort(col("Fiscal Year").desc) 
	return trend
}
def get_trend_NAICS(NAICS:String, df:DataFrame): = DataFrame{
    val trend:DataFrame = df.filter(col("NAICS" === NAICS)).groupBy("Fiscal Year").sum("TotalApproval","TotalDenial","TotalApply").withColumn("ApprovalRate", col("sum(TotalApproval)")/col("sum(TotalApply)")).sort(col("Fiscal Year").desc) 
    return trend
}

val NAICS_54 = cdata.filter(col("NAICS") === 54).groupBy("Employer").sum("Initial Approvals","Initial Denials").sort(col("sum(Initial Approvals)").desc).rdd
val processed = NAICS_54.map( x => (x.getString(0),processCompany(x.getString(0))))
for( x <- processed.take(20)){ println(x)}

//Employer trend (NAICS_54 only)
val pivot_Employer_year = cdata.filter(col("NAICS") === 54).groupBy("Employer").pivot("Fiscal Year").sum("Initial Approvals").sort(col("2019").desc)
// scala> pivot_Employer_year.show(10)
// +--------------------+----+----+----+----+----+----+----+----+----+----+----+   
// |            Employer|2009|2010|2011|2012|2013|2014|2015|2016|2017|2018|2019|
// +--------------------+----+----+----+----+----+----+----+----+----+----+----+
// |          GOOGLE LLC|null|null|null|null|null|null|null|null|null| 732|2701|
// |TATA CONSULTANCY ...|   0|null|   1|   1|   2|   7|4760|2022|2311| 532|1736|
// |COGNIZANT TECH SO...|null|null|null|9194|   4|   9|3843|3932|3206| 501|1581|
// |DELOITTE CONSULTI...| 338| 308| 504|1101| 934| 828| 670| 583| 611| 694|1170|
// |     IBM CORPORATION| 198| 189| 223| 232| 259| 209| 210| 252| 270| 309|1167|
// |TECH MAHINDRA AME...|null|  31|null|   0|   1|1666|1571|1225|2218| 586| 939|
// |CAPGEMINI AMERICA...|   8|  12|  19|  20|   6|  15|   7| 882| 518| 272| 814|
// |       ACCENTURE LLP|  48|  23| 731|4092|3373|2530|3428|1847| 953| 363| 655|
// |       WIPRO LIMITED|2107|1947|2936|4362|2676|3333|3182|1474|1070| 275| 607|
// |LARSEN AND TOUBRO...|null|null|null|null|null|   1|null|   0|   0|null| 537|
// +--------------------+----+----+----+----+----+----+----+----+----+----+----+


//State trend (NAICS_54 only)
val pivot_state_year = cdata.filter(col("NAICS") === 54).groupBy("State").pivot("Fiscal Year").sum("Initial Approvals").sort(col("2019").desc)
pivot_state_year.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/result/pivot_state_year")
// scala> pivot_state_year.show
// +-----+----+-----+-----+-----+-----+-----+-----+-----+----+----+-----+          
// |State|2009| 2010| 2011| 2012| 2013| 2014| 2015| 2016|2017|2018| 2019|
// +-----+----+-----+-----+-----+-----+-----+-----+-----+----+----+-----+
// |   CA|7222| 5235| 9039|11130|11740|10699| 8644| 8598|7967|6611|14294|
// |   NJ|9001|10281|15824|24409|18385|16387|13375|10683|9315|6776| 9119|
// |   TX|3636| 5899| 7471|10926|12329|15555|12223|11801|9087|4160| 7473|
// |   NY|3783| 2587| 3878| 3870| 3666| 3520| 3011| 3484|2749|3436| 4292|
// |   MA|2447| 1650| 2359| 3477| 2625| 2532| 2049| 2123|2045|2517| 3639|
// |   IL|1620| 1148| 3305| 6258| 6140| 5727| 5720| 4203|2716|1782| 3380|
// |   MD|1210| 1027| 2903| 8680| 7541| 8719| 5698| 3015|3012|1401| 2802|
// |   VA|1982| 1101| 1963| 2271| 2627| 3375| 2318| 1998|1663|2153| 2624|
// |   PA|1887| 1315| 2166| 3114| 2657| 2596| 2027| 1703|1705|1899| 2488|
// |   MI| 978|  801| 1750| 3055| 3191| 3673| 2897| 2522|2277|1316| 2111|


//NAICS trend 
val pivot_NAICS_year = cdata.groupBy("NAICS").pivot("Fiscal Year").sum("Initial Approvals")sort(col("2019").desc)
pivot_state_year.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/result/pivot_NAICS_year")
// scala> pivot_NAICS_year.show
// +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+       
// |NAICS| 2009| 2010| 2011| 2012| 2013| 2014| 2015| 2016| 2017| 2018| 2019|
// +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
// |   54|44900|38250|60639|89056|83344|86137|69650|61001|52110|39692|64814|
// |   33| 8572| 6755| 9908|10383| 8802| 7916| 7116| 7427| 8228| 8489|13212|
// |   61|15726|12156|12982|11225|12057|11188|11571|12208|10812|10752|12562|
// |   51| 2568| 4088| 4932| 5480| 4639| 5085| 5098| 5332| 6110| 6203|10313|
// |   52| 4763| 4328| 5147| 4703| 3887| 4071| 4165| 4372| 4011| 5729| 8316|
// |   62| 8404| 6856| 7054| 5621| 5865| 5362| 4920| 5252| 5211| 5032| 6097|
// |   45|  548|  605|  963| 1347| 1440| 1544| 1552| 1939| 3031| 3615| 4734|
// |   32| 1742| 1254| 1693| 1769| 1475| 1347| 1087| 1058| 1032| 1464| 1936|
// |   99| 2391| 1661| 1962| 3019| 2288|  901|  500|  520|  403|  587| 1700|
// |   56| 1486|  902| 1253| 1086| 1066|  932|  807| 1048| 1087| 1030| 1533|