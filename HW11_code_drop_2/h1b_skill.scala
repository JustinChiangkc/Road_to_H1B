import org.apache.spark.sql._
import spark.implicits._


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


val skills = "cleaned_jobs"
val h1b = "final_project/data/cleaned_data"

val skillRdd = sc.textFile(skills)
val h1bdatafram = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv(h1b)

//skillRDD to data frame

import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StringType

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
def processCompanyUDF = udf((text: String) => {
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
val h1b_Employer = h1bdatafram.filter(col("Fiscal Year")>2011)
.groupBy("Employer")
.sum("Initial Approvals","Initial Denials")
.withColumn("TotalApply", col("sum(Initial Approvals)") + col("sum(Initial Denials)"))
.withColumn("Employer",col("Employer").processCompany)
.sort(col("TotalApply").desc)


//Join h1b and skill
val h1b_skill = h1b_Employer.join(skillDF).where(h1b_Employer("Employer") === skillDF("Employer"))
h1bdatafram

//join on employer

//pivot select 