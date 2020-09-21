import org.apache.spark.sql.DataFrame

//load in data
val dir = "final_project/data/cleaned_data"
val cdata = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv(dir)

//functiuons for company name process
def processCompany(text: String): String = {
    var lower = text.toLowerCase()
    
    var paren = """\(.*\)""".r
    lower = paren.replaceAllIn(lower, "")
   
    val suffix = """(inc)|(llc)|(limited)|(incorporation)|(incorporated)|(corporate)|(corporation)|(orporated)|(company)|(lp)|(llp)|(ltd)|(university)|(associate[s]?)|( com )|( co )|( corp )|( com,)|( co,)|( corp,)|(corp)|(group)|(holding)|(lab[s]?)|([\s\.]+co((m)|(rp))*[,\s]+)""".r
    lower = suffix.replaceAllIn(lower, "") 

    val services = """(service[s]?)|(online)|(system[s]?)|(solution[s]?)|(data)|(software)|(infotech)|(digital)|([\s,]info[\s,])|([\s]soft)|(web)|(solns)""".r
    lower = services.replaceAllIn(lower, "")
    
    val industry = """(consulting)|(financial)|(technolog(y|(ies)))|(communication[s]?)|(entertainment)|([\s,]it[$\s,])|(information)|(business)|(network[s]?)|(resource[s]?)|(consultancy)|(svcs)|(tech)""".r
    lower = industry.replaceAllIn(lower, "")

    val location = """(international)|(global)|(america[s]?)|([,\s]us[,\s])|([,\s]us[a]?)""".r
    lower = location.replaceAllIn(lower, "")

    var symbols = """[\.&,-/\*^@#&$%!\?]""".r//deal with space!
    lower = symbols.replaceAllIn(lower, "")

    return lower.trim()
}

def processCompanyHelper(text: String): String = {
    val text1 = text.toLowerCase()
    val text2 = if (text1.contains("amazon")) "amazon" else text1
    val text3 = if (text2.contains("google")) "google" else text2
    val text4 = if (text3.contains("facebook")) "facebook" else text3
    val text5 = if (text4.contains("microsoft")) "microsoft" else text4
    val text6 = if (text5.contains("netflix")) "netflix" else text5
    val text7 = if (text6.contains("vmware")) "vmware" else text6
    val text8 = if (text7.contains("nvidia")) "nvidia" else text7
    val text9 = if (text8.contains("adobe")) "adobe" else text8
    val text10 = if (text9.contains("linkedin")) "linkedin" else text9
    val text11 = if (text10.contains("qualcomm")) "qualcomm" else text10
    val text12 = if (text11.contains("ibm")) "ibm" else text11

    return text10
}

//convert function into UDF
import org.apache.spark.sql.functions.udf
val processCompanyUDF = udf(processCompany _)
val processCompanyHelperUDF = udf(processCompanyHelper _)


//TotalApproval & TotalDenial
val cdata2 = cdata.withColumn("TotalApproval", col("Initial Approvals") + col("Continuing Approvals")).withColumn("TotalDenial", col("Initial Denials") + col("Continuing Denials")).withColumn("TotalApply", col("TotalApproval") + col("TotalDenial"))

//Groupby emplyer, Fiscal Year
// val Employer = cdata2.groupBy("Fiscal Year","Employer").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)
// //Groupby NAICS, Fiscal Year
// val State = cdata2.groupBy("Fiscal Year","State").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)
// //Groupby State, Fiscal Year
// val NAICS = cdata2.groupBy("Fiscal Year","NAICS").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)

//AR_Employer_State
val AR_Year_Employer_State = cdata.filter(col("Fiscal Year")>2011).groupBy("Fiscal Year","State","Employer").sum("Initial Approvals","Initial Denials").withColumn("ApprovalRate", col("sum(Initial Approvals)")/(col("sum(Initial Approvals)")+col("sum(Initial Denials)"))).sort(col("sum(Initial Approvals)").desc).na.drop.filter(col("ApprovalRate") > 0)
AR_Year_Employer_State.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/AR_Year_Employer_State2")

// val rate = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/AR_Year_Employer_State")
// val rate2 = rate.sort(col("ApprovalRate")).na.drop.filter(col("ApprovalRate") > 0).show(5)

//AR_Employer_NAICS
val AR_Year_NAICS_Employer = cdata.withColumn("Employer", processCompanyUDF('Employer)).withColumn("Employer", processCompanyHelperUDF('Employer)).filter(col("Fiscal Year")>2011).groupBy("Fiscal Year","NAICS","Employer").sum("Initial Approvals","Initial Denials").withColumn("ApprovalRate", col("sum(Initial Approvals)")/(col("sum(Initial Approvals)")+col("sum(Initial Denials)"))).sort(col("sum(Initial Approvals)").desc).na.drop.filter(col("ApprovalRate") > 0).filter(col("sum(Initial Approvals)") > 10)
AR_Year_NAICS_Employer.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/AR_Year_NAICS_Employer")

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

// val NAICS_54 = cdata.filter(col("NAICS") === 54).groupBy("Employer").sum("Initial Approvals","Initial Denials").sort(col("sum(Initial Approvals)").desc).rdd
// val processed = NAICS_54.map( x => (x.getString(0),processCompany(x.getString(0))))
// for( x <- processed.take(20)){ println(x)}

//Check function
// val processCompanyUDF = udf(processCompany _)
// val processCompanyHelperUDF = udf(processCompanyHelper _)
// val test = cdata.withColumn("Employer2", processCompanyUDF('Employer)).withColumn("Employer2", processCompanyHelperUDF('Employer)).select("Employer","Employer2")
// test.createOrReplaceTempView("test")
// spark.sql("SELECT distinct(*) FROM test where Employer2 like '%apple%'").show(false)
// +----------------------------------+---------+                                  
// |Employer                          |Employer2|
// +----------------------------------+---------+
// |AMAZON MEDIA GRP LLC              |amazon   |
// |AMAZON WEB SREVICES INC           |amazon   |
// |AMAZON DIGITAL SERVICES LLC       |amazon   |
// |AMAZON.COM.KYDC LLC               |amazon   |
// |AMAZON DIGITAL SVCS INC           |amazon   |
// |AMAZONIC VENTURES LLC             |amazon   |
// |AMAZON TECHNOLOGIES INC           |amazon   |
// |AMAZON PRODUCE NETWORK LLC        |amazon   |
// |AMAZON MEDIA GROUP LLC D/B/A AMAZO|amazon   |
// |AMAZON CARGO INC                  |amazon   |
// |AMAZON LOGISTICS GROUP LLC        |amazon   |
// |AMAZON WEB SVCS INC               |amazon   |
// |AMAZON CORP LLC                   |amazon   |
// |AMAZONIA CONSULTING GR LLC DBA GLO|amazon   |
// |AMAZON CAPITAL SVCS INC           |amazon   |
// |AMAZON COM DEDC LLC               |amazon   |
// |AMAZON DIGITAL SERVICES INC       |amazon   |
// |AMAZON.COM KYDC LLC               |amazon   |
// |AMAZON PHARMACY INC               |amazon   |
// |AMAZON WEB SERVICE INC            |amazon   |
// +----------------------------------+---------+
// +-----------------------------+---------+                                       
// |Employer                     |Employer2|
// +-----------------------------+---------+
// |FACEBOOK MIAMI INC           |facebook |
// |FACEBOOK SERVICES INC        |facebook |
// |FACEBOOK SVCS INC            |facebook |
// |FACEBOOKSTER INC DBA AVENUESO|facebook |
// |FACEBOOK PAYMENTS INC        |facebook |
// |FACEBOOK INC                 |facebook |
// +-----------------------------+---------+
// +--------------------------------+---------+                                    
// |Employer                        |Employer2|
// +--------------------------------+---------+
// |GOOGLE VENTURES MGMT CO LLP     |google   |
// |GOOGLEPLEX D/B/AALTERNATIVEPATHS|google   |
// |GOOGLE LIFE SCIENCES LLC        |google   |
// |GOOGLE VENTURES MANAGMENT CO LLC|google   |
// |GOOGLE CAPITAL MGMT COMPANY LLC |google   |
// |GOOGLE INC                      |google   |
// |GOOGLE (ITA SOFTWARE INC)       |google   |
// |GOOGLE LLC                      |google   |
// +--------------------------------+---------+
// // +--------------------+--------------------+                                     
// // |            Employer|           Employer2|
// // +--------------------+--------------------+
// // |       LINKEDIN CORP|       linkedin corp|
// // |LINKEDIN CORPORATION|linkedin corporation|
// // +--------------------+--------------------+
// +-------------------------------+---------+                                     
// |Employer                       |Employer2|
// +-------------------------------+---------+
// |MICROSOFT LICENSING GP         |microsoft|
// |MICROSOFT PAYMENTS INC         |microsoft|
// |MICROSOFT OPEN TECHNOLOGIES INC|microsoft|
// |MICROSOFT CORPORATIOIN         |microsoft|
// |MICROSOFT OPS PUERTO RICO LLC  |microsoft|
// |MICROSOFT CARIBBEAN INC        |microsoft|
// |MICROSOFT CORP                 |microsoft|
// |MICROSOFT                      |microsoft|
// |MICROSOFT ONLINE INC           |microsoft|
// |MICROSOFT CORPORATION          |microsoft|
// +-------------------------------+---------+
// +----------------------------------+----------------------------------+         
// |Employer                          |Employer2                         |
// +----------------------------------+----------------------------------+
// |DIGITAL INTELLIGENCE SYSTEMS LLC  |digital intelligence systems llc  |
// |INTELLICORP INC                   |intellicorp inc                   |
// |IRON MTN INTELLECTUAL PROPERTY MGT|iron mtn intellectual property mgt|
// |SOUNDMIND INTELLIGENCE INC        |soundmind intelligence inc        |
// |INTELLI-MARK TECHS INC DBA ETIX   |intelli-mark techs inc dba etix   |
// |INTELIANT TECHNOLOGIES            |inteliant technologies            |
// |GAP INTELLIGENCE INC              |gap intelligence inc              |
// |EMBODIED INTELLIGENCE INC D/B/A CO|embodied intelligence inc d/b/a co|
// |MOBILE INTELLIGENCE CORPORATION   |mobile intelligence corporation   |
// |INTELLIGENT FUSION TECHNOLOGY     |intelligent fusion technology     |
// |INTUPOINT INC D/B/A POINTEL       |intupoint inc d/b/a pointel       |
// |INTELLIGENT SWITCHGEAR ORGS LLC   |intelligent switchgear orgs llc   |
// |INTELLISWIFT SOFTWARE INC         |intelliswift software inc         |
// |SYSINTELLI INC                    |sysintelli inc                    |
// |INTELLECT SOLUTIONS LLC           |intellect solutions llc           |
// |INSIGHT INTELLI INC               |insight intelli inc               |
// |HEALTH INTELLIGENCE COMPANY LLC   |health intelligence company llc   |
// |PROGRESSIVE INTELLIGENCE TECHNOLOG|progressive intelligence technolog|
// |ONASSET INTELLIGENCE INC          |onasset intelligence inc          |
// |TARA INTELLIGENCE INC             |tara intelligence inc             |
// +----------------------------------+----------------------------------+
// +----------------------------------+----------------------------------+         
// |Employer                          |Employer2                         |
// +----------------------------------+----------------------------------+
// |APPLE SOFT INC                    |apple soft inc                    |
// |ISOAPPLE INC                      |isoapple inc                      |
// |APPLE ALUM USA CORP               |apple alum usa corp               |
// |APPLE DENTIST PLLC                |apple dentist pllc                |
// |PINEAPPLE CO LLC                  |pineapple co llc                  |
// |AMAZING APPLE INC                 |amazing apple inc                 |
// |APPLEONE                          |appleone                          |
// |APPLE EXHIBITS CORP               |apple exhibits corp               |
// |TINYAPPLE LLC                     |tinyapple llc                     |
// |APPLETREE DAY CARE CENTER INC     |appletree day care center inc     |
// |APPLEDORE MED GR INC DBA COASTAL N|appledore med gr inc dba coastal n|
// |GOOD APPLE PUBLISHING LLC         |good apple publishing llc         |
// |APPLE TREE EARLY LEARNING PUBLIC C|apple tree early learning public c|
// |APPLECRATE INC                    |applecrate inc                    |
// |BIG APPLE SIGN CORP DBA BIG APPLE |big apple sign corp dba big apple |
// |APPLE BANNER INC                  |apple banner inc                  |
// |APPLECARE HOSPITALIST MED GROUP IN|applecare hospitalist med group in|
// |BIG APPLE SIGN CORPORATION        |big apple sign corporation        |
// |RBEX INC DBA APPLE TOWING COMPANY |rbex inc dba apple towing company |
// |APPLE MEDICAL CTR AND URGENT CARE |apple medical ctr and urgent care |
// +----------------------------------+----------------------------------+



//Employer trend 
val sqlContext = new SQLContext(sc)
import sqlContext.implicits._
import org.apache.spark.sql._

val pivot_Employer_year = cdata.withColumn("Employer", processCompanyUDF('Employer)).rdd.map(row => {
    val row1 = row.getAs[String](1)
    val make = if (row1.toLowerCase == "amazoncom") "amazon" else row1
    Row(row(0),make,row(2),row(3),row(4),row(5),row(6),row(7))
  })
val pivot_Employer_year = cdata.withColumn("Employer", processCompanyUDF('Employer)).withColumn("Employer", processCompanyHelperUDF('Employer)).groupBy("Employer").pivot("Fiscal Year").sum("Initial Approvals").sort(col("2019").desc)
val pivot_Employer_year2 = pivot_Employer_year.withColumn("Sum",col("2009")+col("2010")+col("2011")+col("2012")+col("2013")+col("2014")+col("2015")+col("2016")+col("2017")+col("2018")+col("2019")).sort(col("Sum").desc)
pivot_Employer_year2.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/result/pivot_Employer_year")
// +--------------------+----+----+----+----+----+----+----+----+----+----+----+-----+
// |            Employer|2009|2010|2011|2012|2013|2014|2015|2016|2017|2018|2019|  Sum|
// +--------------------+----+----+----+----+----+----+----+----+----+----+----+-----+
// |           cognizant| 234|4483|5668|9535|5422|5448|3846|3935|3208| 503|1590|43872|
// |                tata|  18| 197|1754|7621|6446|7550|4771|2061|2363| 548|1785|35114|
// |             infosys| 459|3823|3890|5604|6433|4137|2771|2348|1181|  74| 768|31488|
// |               wipro|2109|1948|2938|4363|2676|3333|3185|1475|1235| 283| 608|24153|
// |           accenture| 339|  28|1354|4093|3374|2531|3433|1849| 955| 365| 656|18977|
// |              amazon| 214| 271| 427| 788| 889| 924|1067|1420|2499|2858|3570|14927|
// |           microsoft|1503|1940|1407|1514|1056| 900| 969|1141|1471|1258|1706|14865|
// |                 hcl|  43|  75|1119|2325|1962|1004|1367|1077| 918| 209| 455|10554|
// |              google| 241| 307| 595| 661| 762| 768| 848| 938|1214| 784|2701| 9819|
// |            mahindra|  47|  31|  67| 759| 472|1666|1571|1226|2220| 590| 947| 9596|
// |   ibm india private|  82| 895| 739| 749|1388|1347|1713|1343| 956|  62|  86| 9360|
// |               intel| 817| 500| 830| 826| 811| 730| 636| 940|1092| 904|1035| 9121|
// |      larsen  toubro| 673| 370|1250|1867|1604|1379| 561| 874| 211| 155|   0| 8944|
// |            deloitte| 346| 312| 515|1105| 938| 836| 672| 588| 617| 701|1185| 7815|
// |              syntel| 135| 150| 413|1212|1044|1156|1053| 579| 639| 168| 252| 6801|
// |               apple| 193| 178| 351| 315| 297| 483| 528| 634| 672| 701|1150| 5502|
// |            facebook|  54|  74| 139| 317| 347| 297| 422| 473| 728| 667|1531| 5049|
// |           capgemini|  21|  90| 122|  64| 511| 731| 548|1155| 537| 273| 815| 4867|
// |pricewaterhouseco...| 120| 118| 424| 614| 440| 521| 650| 511| 349| 127| 336| 4210|
// |            qualcomm| 374| 340| 319| 602| 817| 404| 218| 144| 301| 126| 543| 4188|
// +--------------------+----+----+----+----+----+----+----+----+----+----+----+-----+

//check
val pivot_Employer_year3 = cdata.withColumn("Employer", processCompanyUDF('Employer)).withColumnRenamed("Initial Approvals","IA")
pivot_Employer_year3.createOrReplaceTempView("pivot_Employer_year3")
spark.sql("SELECT * FROM pivot_Employer_year3 where Employer like '%amazon%' order by IA desc").show()
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
val pivot_state_year = cdata.withColumn("Employer", processCompanyUDF('Employer)).withColumn("Employer", processCompanyHelperUDF('Employer)).groupBy("State").pivot("Fiscal Year").sum("Initial Approvals").sort(col("2019").desc)
pivot_state_year.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/result/pivot_state_year")
// scala> pivot_state_year.show
// +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+       
// |State| 2009| 2010| 2011| 2012| 2013| 2014| 2015| 2016| 2017| 2018| 2019|
// +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
// |   CA|16948|13179|19156|22136|21949|20535|17986|18593|17218|15601|31148|
// |   TX| 8341| 9425|11850|15336|16477|19250|15652|14905|12392| 6933|11631|
// |   NJ|11551|12381|18238|26680|20092|18092|14752|12205|10698| 8885|11600|
// |   NY|11484| 9517|12178|10634| 9541| 9031| 8079| 8421| 7249| 9431|11149|
// |   WA| 3007| 3258| 3128| 3588| 3269| 3123| 3274| 3752| 5205| 5335| 7438|
// |   MA| 4743| 3491| 4601| 5669| 4732| 4583| 4019| 4384| 4044| 5042| 6900|
// |   IL| 4139| 3065| 5570| 8443| 8257| 7820| 7692| 6141| 4460| 3771| 6408|
// |   MI| 2572| 2101| 3178| 4410| 4625| 5040| 4151| 4171| 3966| 2908| 4343|
// |   PA| 4313| 2895| 4010| 4791| 4256| 3963| 3367| 3066| 3072| 3578| 4336|
// |   VA| 3119| 1900| 2897| 3234| 3495| 4220| 3125| 2773| 2443| 2990| 3632|
// |   MD| 2536| 1919| 3951| 9525| 8334| 9445| 6407| 3694| 3580| 2147| 3576|
// |   FL| 3910| 3154| 4069| 3834| 3222| 3475| 3036| 3055| 2838| 2125| 3404|
// |   NC| 2425| 2090| 2371| 3188| 2833| 2787| 3058| 2672| 2245| 1581| 3214|
// |   GA| 2612| 1626| 2509| 2436| 3132| 3225| 2831| 2531| 2184| 2011| 2766|
// |   AZ| 1039|  707| 1020|  997|  801|  819|  712|  684| 1791| 1764| 2261|
// |   OH| 1914| 1349| 1710| 1776| 1870| 1791| 1527| 1801| 1439| 1294| 1805|
// |   TN|  905|  732| 1136| 1022|  948|  838|  793|  848|  819| 1223| 1538|
// |   CT| 1435| 1099| 1276| 1428| 1236| 1241| 1185| 1049|  919| 1162| 1445|
// |   MO|  978|  710|  891|  938| 1055| 1141|  833|  932|  866|  835| 1431|
// |   MN|  931|  669|  895|  928|  941|  863|  815|  836|  827|  875| 1346|
// +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+


//NAICS trend 
val pivot_NAICS_year = cdata.withColumn("Employer", processCompanyUDF('Employer)).withColumn("Employer", processCompanyHelperUDF('Employer)).groupBy("NAICS").pivot("Fiscal Year").sum("Initial Approvals").sort(col("2019").desc)
pivot_NAICS_year.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/result/pivot_NAICS_year")
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
// |   42|  756|  589|  834|  882|  900| 1188|  977|  933|  688|  950| 1362|
// |   23|  416|  257|  347|  328|  350|  462|  475|  453|  440|  718| 1055|
// |   44| 1249|  901|  927|  766|  642|  639|  531|  520|  441|  569|  813|
// |   53|  352|  287|  335|  303|  230|  340|  273|  289|  248|  351|  634|
// |   55|  257|  288|  344|  333|  307|  359|  402|  363|  319|  414|  586|
// |   31|  788|  681|  861|  717|  537|  523|  460|  371|  318|  359|  569|
// |   81|  812|  732|  855|  702|  535|  531|  467|  424|  318|  370|  432|
// |   48|  569|  397|  420|  428|  382|  350|  289|  278|  231|  303|  377|
// |   21|  763|  483|  636|  892|  671|  508|  374|  177|  221|  182|  363|
// |   22|  435|  298|  286|  302|  244|  218|  210|  213|  162|  190|  294|
// +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+

