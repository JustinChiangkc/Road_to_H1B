import org.apache.spark.sql.DataFrame

//load in data
val dir = "final_project/h1b_acceptance/cleaned_h1bdata"
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


//Check TotalApproval & TotalDenial
val cdata2 = cdata.withColumn("TotalApproval", col("Initial Approvals") + col("Continuing Approvals")).withColumn("TotalDenial", col("Initial Denials") + col("Continuing Denials")).withColumn("TotalApply", col("TotalApproval") + col("TotalDenial"))

//Groupby emplyer, Fiscal Year
val Employer = cdata2.groupBy("Fiscal Year","Employer").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)
//Groupby NAICS, Fiscal Year
val State = cdata2.groupBy("Fiscal Year","State").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)
//Groupby State, Fiscal Year
val NAICS = cdata2.groupBy("Fiscal Year","NAICS").sum("TotalApproval","TotalDenial","TotalApply").sort(col("Fiscal Year"),col("sum(TotalApply)").desc)

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
// val AR_Employer = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/AR_Employer2")
// val AR_State = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/AR_State2")
// val AR_NAICS = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/AR_NAICS2")



//Functions for Getting trend for Employer/State/NAICS
def get_trend_Employer(Employer:String, df:DataFrame): DataFrame = {
	val trend:DataFrame = df.filter(col("Employer") === Employer)
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

//Check NAICS_54
val NAICS_54 = cdata.filter(col("NAICS") === 54).groupBy("Employer").sum("Initial Approvals","Initial Denials").sort(col("sum(Initial Approvals)").desc).rdd
val processed = NAICS_54.map( x => (x.getString(0),processCompany(x.getString(0))))
for( x <- processed.take(20)){ println(x)}

//Check function
val processCompanyUDF = udf(processCompany _)
val processCompanyHelperUDF = udf(processCompanyHelper _)
val test = cdata.withColumn("Employer2", processCompanyUDF('Employer)).withColumn("Employer2", processCompanyHelperUDF('Employer)).select("Employer","Employer2")
test.createOrReplaceTempView("test")
spark.sql("SELECT distinct(*) FROM test where Employer2 like '%apple%'").show(false)
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

