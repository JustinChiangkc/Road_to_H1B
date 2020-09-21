import org.apache.spark.sql.types._
import spark.implicits._

//functions
def processCompany(text: String): String = {
    var lower = text.toLowerCase()
    var symbols = """[\.&,-/\*^@#&$%!\?]""".r//deal with space!
    lower = symbols.replaceAllIn(lower, "")
    
    var paren = """\(.*\)""".r
    lower = paren.replaceAllIn(lower, "")
   
    val suffix = """(inc)|(llc)|(limited)|(incorporation)|(incorporated)|(corporate)|(corporation)|(orporated)|(company)|(lp)|(llp)|(ltd)|(university)|(associate[s]?)|( com )|( co )|( corp )|( com,)|( co,)|( corp,)|(corp)|(group)|(holding)|(lab[s]?)""".r
    lower = suffix.replaceAllIn(lower, "") 

    val services = """(service[s]?)|(online)|(system[s]?)|(solution[s]?)|(data)|(software)|(infotech)|(digital)|([\s,]info[\s,])|([\s]soft)|(web)|(solns)""".r
    lower = services.replaceAllIn(lower, "")
    
    val industry = """(consulting)|(financial)|(technolog(y|(ies)))|(communication[s]?)|(entertainment)|([\s,]it[$\s,])|(information)|(business)|(network[s]?)|(resource[s]?)|(consultancy)|(svcs)|(tech)""".r
    lower = industry.replaceAllIn(lower, "")

    val location = """(international)|(global)|(america[s]?)|([,\s]us[,\s])|([,\s]us[a]?)""".r
    lower = location.replaceAllIn(lower, "")
    return lower.trim()
}

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer 
def parseSkills(text: String): String = {
    val bitmask = text.toInt 
    var ret = new ListBuffer[String]()
    val lookup = HashMap(0 -> "C", 1 -> "C++", 2 -> "Python", 3 -> "Java", 4 -> "Javascript", 5 -> "Go", 6 -> "Scala", 7 -> "C#", 8 -> "SQL")
    var a = 0;

    for (a <- 0 to 8){
        if (((1 << a) & bitmask) != 0){
            ret += lookup(a)
        }
    }
    return ret.mkString(",")
}

//load data
val skills = "cleaned_jobs"
val h1b = "final_project/data/cleaned_data"

val skillRdd = sc.textFile(skills)
val h1bdatafram = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv(h1b)

//skillRDD to data frame
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql._

val skillParsed = skillRdd.map(line => line.replace("(", "").replace(")", "").split(","))
val skill_rowRDD = skillParsed.map(line => Row(processCompany(line(0)), line(1),line(2),line(3)))
val skillSchema = List(
  StructField("Employer", StringType, true),
  StructField("Title", StringType, true),
  StructField("PL", StringType, true),
  StructField("Degree", StringType, true)
)
val skillDF = spark.createDataFrame(
  skill_rowRDD,
  StructType(skillSchema)
)


//h1b groupby employer
import org.apache.spark.sql.functions.udf
val processCompanyUDF = udf(processCompany _)
val h1b_Employer = h1bdatafram.filter(col("Fiscal Year")>2011).withColumn("Employer",processCompanyUDF('Employer)).groupBy("Employer").sum("Initial Approvals","Initial Denials").withColumn("TotalApply", col("sum(Initial Approvals)") + col("sum(Initial Denials)")).sort(col("TotalApply").desc)

//check 
import org.apache.spark.sql.functions.countDistinct
h1b_Employer.agg(countDistinct("Employer")).show
// +------------------------+                                                      
// |count(DISTINCT Employer)|
// +------------------------+
// |                  188416|
// +------------------------+

//Join h1b and skill
// val h1b_skill = h1b_Employer.join(skillDF).where(h1b_Employer("Employer") === skillDF("Employer"))
val h1b_skill = h1b_Employer.join(skillDF,Seq("Employer"),"inner")

//Save data
h1b_skill.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/h1b_skill2")
//load data
val dir = "final_project/data/h1b_skill2"
val h1b_skill = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv(dir)

//for each PL, get h1b count
// import scala.collection.mutable.Map

// val h1b_skillSQL = h1b_skill.withColumnRenamed("sum(Initial Approvals)", "IP").withColumnRenamed("sum(Initial Denials)", "ID")
// h1b_skillSQL.createOrReplaceTempView("h1b_skill_view")

// // val PL_h1b = Map[String, String]()
// // val IP = spark.sql("SELECT sum(IP) FROM h1b_skill_view WHERE PL LIKE '%0%' ").collect
// // val PL_h1b0 = PL_h1b + ("C" -> IP(0)(0).toString)
// // val IP = spark.sql("SELECT sum(IP) FROM h1b_skill_view WHERE PL LIKE '%1%' ").collect
// // val PL_h1b1 = PL_h1b0 + ("C++" -> IP(0)(0).toString)
// // val IP = spark.sql("SELECT sum(IP) FROM h1b_skill_view WHERE PL LIKE '%2%' ").collect
// // val PL_h1b2 = PL_h1b1 + ("Python" -> IP(0)(0).toString)
// // val IP = spark.sql("SELECT sum(IP) FROM h1b_skill_view WHERE PL LIKE '%3%' ").collect
// // val PL_h1b3 = PL_h1b2 + ("Java" -> IP(0)(0).toString)
// // val IP = spark.sql("SELECT sum(IP) FROM h1b_skill_view WHERE PL LIKE '%4%' ").collect
// // val PL_h1b4 = PL_h1b3 + ("Javascript" -> IP(0)(0).toString)
// // val IP = spark.sql("SELECT sum(IP) FROM h1b_skill_view WHERE PL LIKE '%5%' ").collect
// // val PL_h1b5 = PL_h1b4 + ("Go" -> IP(0)(0).toString)
// // val IP = spark.sql("SELECT sum(IP) FROM h1b_skill_view WHERE PL LIKE '%6%' ").collect
// // val PL_h1b6 = PL_h1b5 + ("Scala" -> IP(0)(0).toString)
// // val IP = spark.sql("SELECT sum(IP) FROM h1b_skill_view WHERE PL LIKE '%7%' ").collect
// // val PL_h1b7 = PL_h1b6 + ("C#" -> IP(0)(0).toString)
// // val IP = spark.sql("SELECT sum(IP) FROM h1b_skill_view WHERE PL LIKE '%8%' ").collect
// // val PL_h1b8 = PL_h1b7 + ("SQL" -> IP(0)(0).toString)
// import spark.implicits._
// val PLdf = PL_h1b8.toSeq.toDF("PL", "h1bCount")
// PLdf.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/PLdf.csv")

import org.apache.spark.sql.functions.udf
val parseSkillsUDF = udf(parseSkills _)
val PL_h1b = h1b_skill.withColumn("PL",parseSkillsUDF('PL)).withColumn("PL",split(col("PL"),",")).select($"sum(Initial Approvals)",explode($"PL")).groupBy("col").sum("sum(Initial Approvals)").withColumnRenamed("col","ProgrammingLanguage").withColumnRenamed("sum(sum(Initial Approvals))","h1bCount").filter(col("ProgrammingLanguage") =!= "").withColumn("h1bCount",$"h1bCount"/8)
PL_h1b.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/PL_h1b2.csv")
// +-------------------+--------+
// |ProgrammingLanguage|h1bCount|
// +-------------------+--------+
// |C#                 |82519   |
// |C++                |58133   |
// |Javascript         |57309   |
// |C                  |47102   |
// |Scala              |69654   |
// |SQL                |189453  |
// |Go                 |2604    |
// |Python             |143682  |
// |Java               |92883   |
// +-------------------+--------+

// +-------------------+---------+ per year
// |ProgrammingLanguage| h1bCount|
// +-------------------+---------+
// |                 C#|10314.875|
// |                C++| 7266.625|
// |         Javascript| 7163.625|
// |                  C|  5887.75|
// |              Scala|  8706.75|
// |                SQL|23681.625|
// |                 Go|    325.5|
// |             Python| 17960.25|
// |               Java|11610.375|
// +-------------------+---------+

