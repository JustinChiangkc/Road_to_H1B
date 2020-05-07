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
import org.apache.spark.sql.functions.udf
val processCompanyUDF = udf(processCompany _)
val processCompanyHelperUDF = udf(processCompanyHelper _)


//load data
val skills = "cleaned_jobs2"
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
val h1b_Employer = h1bdatafram.filter(col("Fiscal Year")>2011).withColumn("Employer",processCompanyUDF('Employer)).withColumn("Employer", processCompanyHelperUDF('Employer)).groupBy("Employer").sum("Initial Approvals","Initial Denials").withColumn("TotalApply", col("sum(Initial Approvals)") + col("sum(Initial Denials)")).sort(col("TotalApply").desc)

//Join h1b and skill
val h1b_skill = h1b_Employer.join(skillDF,Seq("Employer"),"inner")
h1b_skill.persist

// //Save data
// h1b_skill.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/h1b_skill3")
// //load data
// val dir = "final_project/data/h1b_skill3"
// val h1b_skill = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv(dir)


//skill groups
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
def parseSkills2(text: String): String = {
    val bitmask = text.toInt
    var ret = new ListBuffer[String]()
    var a = 0;
    var (cc, bd, dp, db, be, fe, dl) = (false, false, false, false, false, false, false)
    for (a <- 0 to 30){
        if (((1 << a) & bitmask) != 0){
            if (a <= 2){
                if (!cc){
                    ret += "Cloud Computing"
                    cc = true
                }
            } else if (a <= 7){
                if (!bd){
                    ret += "Big Data"
                    bd = true
                }
            } else if (a <= 11){
                if (!dp){
                    ret += "DevOp"
                    dp = true
                }
            } else if (a <= 18){
                if (!db){
                    ret += "Database"
                    db = true
                }
            } else if (a <= 24){
                if (!be){
                    ret += "Back end"
                    be = true
                }
            } else if (a <= 27){
                if (!fe){
                    ret += "Front end"
                    fe = true
                }
            } else{
                if (!dl){
                    ret += "Deep Learning"
                    dl = true
                }
            }
        }
    }
    return ret.mkString(",")
}
val parseSkillsUDF2 = udf(parseSkills2 _)
val Skills_h1b = h1b_skill.withColumn("PL",parseSkillsUDF2('PL)).withColumn("PL",split(col("PL"),",")).select($"sum(Initial Approvals)",explode($"PL")).groupBy("col").sum("sum(Initial Approvals)").withColumnRenamed("col","Skills").withColumnRenamed("sum(sum(Initial Approvals))","h1bCount").filter(col("Skills") =!= "").withColumn("h1bCount",$"h1bCount"/8)
Skills_h1b.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("data/Skills_h1b")

// +---------------+---------+                                                     
// |         Skills| h1bCount|
// +---------------+---------+
// |      Front end|12280.625|
// |Cloud Computing| 17343.75|
// |  Deep Learning|   4467.0|
// |       Big Data|  11283.0|
// |       Database| 7512.375|
// |       Back end|10116.375|
// |          DevOp|  2652.25|
// +---------------+---------+

//Cloud computing
def parseSkills3(text: String): String = {
    val bitmask = text.toInt
    var ret = new ListBuffer[String]()
    var a = 0;
    for (a <- 0 to 2){
        if (((1 << a) & bitmask) != 0){
            if (a == 0){
                ret += "AWS" 
            } else if (a == 1){
                ret += "Azure"
            } else {
                ret += "GCP"
            }
        }
    }
    return ret.mkString(",")
}
val parseSkillsUDF3 = udf(parseSkills3 _)
val CC_h1b = h1b_skill.withColumn("PL",parseSkillsUDF3('PL)).withColumn("PL",split(col("PL"),",")).select($"sum(Initial Approvals)",explode($"PL")).groupBy("col").sum("sum(Initial Approvals)").withColumnRenamed("col","Skills").withColumnRenamed("sum(sum(Initial Approvals))","h1bCount").filter(col("Skills") =!= "").withColumn("h1bCount",$"h1bCount"/8)
CC_h1b.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("data/CC_h1b2")
// CC_h1b.show(false)
// +------+---------+                                                              
// |Skills|h1bCount |
// +------+---------+
// |AWS   |11921.125|
// |Azure |10275.0  |
// |GCP   |2168.375 |
// +------+---------+

//Big data skills
def parseSkills4(text: String): String = {
    val bitmask = text.toInt
    var ret = new ListBuffer[String]()
    var hm = HashMap(3 -> "Kafka", 4 -> "Elasticsearch", 5 -> "Spark", 6 -> "Hadoop", 7 -> "MapReduce")
    var a = 0;
    for (a <- 3 to 7){
        if (((1 << a) & bitmask) != 0){
            ret += hm(a)
        }
    }
    return ret.mkString(",")
}

val parseSkillsUDF4 = udf(parseSkills4 _)
val BD_h1b = h1b_skill.withColumn("PL",parseSkillsUDF4('PL)).withColumn("PL",split(col("PL"),",")).select($"sum(Initial Approvals)",explode($"PL")).groupBy("col").sum("sum(Initial Approvals)").withColumnRenamed("col","Skills").withColumnRenamed("sum(sum(Initial Approvals))","h1bCount").filter(col("Skills") =!= "").withColumn("h1bCount",$"h1bCount"/8)
BD_h1b.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("data/BD_h1b2")
// +-------------+--------+                                                        
// |Skills       |h1bCount|
// +-------------+--------+
// |MapReduce    |655.125 |
// |Kafka        |5086.125|
// |Elasticsearch|1760.5  |
// |Spark        |7760.75 |
// |Hadoop       |5601.375|
// +-------------+--------+

//DevOps
def parseSkills5(text: String): String = {
    val bitmask = text.toInt
    var ret = new ListBuffer[String]()
    var hm = HashMap(8 -> "Docker", 9 -> "Kubernetes", 10 -> "Jenkins", 11 -> "Maven")
    var a = 0;
    for (a <- 8 to 11){
        if (((1 << a) & bitmask) != 0){
            ret += hm(a)
        }
    }
    return ret.mkString(",")
}

val parseSkillsUDF5 = udf(parseSkills5 _)
val Docker_h1b = h1b_skill.withColumn("PL",parseSkillsUDF5('PL)).withColumn("PL",split(col("PL"),",")).select($"sum(Initial Approvals)",explode($"PL")).groupBy("col").sum("sum(Initial Approvals)").withColumnRenamed("col","Skills").withColumnRenamed("sum(sum(Initial Approvals))","h1bCount").filter(col("Skills") =!= "").withColumn("h1bCount",$"h1bCount"/8)
Docker_h1b.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("data/Docker_h1b2")
// +----------+--------+                                                           
// |Skills    |h1bCount|
// +----------+--------+
// |Docker    |1349.0  |
// |Maven     |518.75  |
// |Kubernetes|1106.125|
// |Jenkins   |1874.125|
// +----------+--------+


//Database
def parseSkills6(text: String): String = {
    val bitmask = text.toInt
    var ret = new ListBuffer[String]()
    var a = 0;
    for (a <- 6 to 10){
        if (((1 << a) & bitmask) != 0){
            if (a == 6){
                ret += "PostgreSQL"
            } else if (a == 7){
                ret += "MySQL"
            } else if (a == 8){
                ret += "MongoDB"
            } else if (a == 9){
                ret += "Cassandra"
            } else {
                ret += "Redis"
            }
        }
    }
    return ret.mkString(",")
}
val parseSkillsUDF6 = udf(parseSkills6 _)
val DB_h1b = h1b_skill.withColumn("PL",parseSkillsUDF6('PL)).withColumn("PL",split(col("PL"),",")).select($"sum(Initial Approvals)",explode($"PL")).groupBy("col").sum("sum(Initial Approvals)").withColumnRenamed("col","Skills").withColumnRenamed("sum(sum(Initial Approvals))","h1bCount").filter(col("Skills") =!= "").withColumn("h1bCount",$"h1bCount"/8)
DB_h1b.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("data/DB_h1b")
// +----------+--------+                                                           
// |    Skills|h1bCount|
// +----------+--------+
// |   MongoDB| 1860.75|
// |     MySQL|2287.875|
// | Cassandra|  1078.0|
// |PostgreSQL| 1750.75|
// |     Redis|1039.125|
// +----------+--------+

//Back-end
def parseSkills7(text: String): String = {
    val bitmask = text.toInt
    var ret = new ListBuffer[String]()
    var a = 0;
    for (a <- 11 to 16){
        if (((1 << a) & bitmask) != 0){
            if (a == 11){
                ret += ".NET"
            } else if (a == 12){
                ret += "Django"
            } else if (a == 13){
                ret += "Flask"        
            } else if (a == 14){
                ret += "Spring"
            } else if (a == 15){
                ret += "Node.JS"
            } else {
                ret += "Express.JS"
            }
        }
    }
    return ret.mkString(",")
}
val parseSkillsUDF7 = udf(parseSkills7 _)
val backend_h1b = h1b_skill.withColumn("PL",parseSkillsUDF7('PL)).withColumn("PL",split(col("PL"),",")).select($"sum(Initial Approvals)",explode($"PL")).groupBy("col").sum("sum(Initial Approvals)").withColumnRenamed("col","Skills").withColumnRenamed("sum(sum(Initial Approvals))","h1bCount").filter(col("Skills") =!= "").withColumn("h1bCount",$"h1bCount"/8)
backend_h1b.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("data/backend_h1b")
// +----------+--------+                                                           
// |    Skills|h1bCount|
// +----------+--------+
// |   Node.JS|5362.875|
// |     Flask| 224.875|
// |    Spring|2208.875|
// |Express.JS| 222.625|
// |      .NET|  6558.0|
// |    Django| 220.625|
// +----------+--------+


//front-end
def parseSkills8(text: String): String = {
    val bitmask = text.toInt
    var ret = new ListBuffer[String]()
    var a = 0;
    for (a <- 17 to 19){
        if (((1 << a) & bitmask) != 0){
            if (a == 17){
                ret += "Angular.JS"
            } else if (a == 18){
                ret += "Vue.JS"
            } else {
                ret += "React.JS/React Native"
            }
        }
    }
    return ret.mkString(",")
}
val parseSkillsUDF8 = udf(parseSkills8 _)
val frontend_h1b = h1b_skill.withColumn("PL",parseSkillsUDF8('PL)).withColumn("PL",split(col("PL"),",")).select($"sum(Initial Approvals)",explode($"PL")).groupBy("col").sum("sum(Initial Approvals)").withColumnRenamed("col","Skills").withColumnRenamed("sum(sum(Initial Approvals))","h1bCount").filter(col("Skills") =!= "").withColumn("h1bCount",$"h1bCount"/8)
frontend_h1b.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("data/frontend_h1b")
// +--------------------+--------+                                                 
// |              Skills|h1bCount|
// +--------------------+--------+
// |React.JS/React Na...|  1558.0|
// |          Angular.JS|10564.25|
// |              Vue.JS|   597.0|
// +--------------------+--------+

//MLDL
def parseSkills9(text: String): String = {
    val bitmask = text.toInt
    var ret = new ListBuffer[String]()
    var a = 0;
    for (a <- 20 to 22){
        if (((1 << a) & bitmask) != 0){
            if (a == 20){
                ret += "Pytorch"
            } else if (a == 21){
                ret += "Tensorflow"
            } else {
                ret += "Scikit-learn"
            }
        }
    }
    return ret.mkString(",")
}
val parseSkillsUDF9 = udf(parseSkills9 _)
val MLDL_h1b = h1b_skill.withColumn("PL",parseSkillsUDF9('PL)).withColumn("PL",split(col("PL"),",")).select($"sum(Initial Approvals)",explode($"PL")).groupBy("col").sum("sum(Initial Approvals)").withColumnRenamed("col","Skills").withColumnRenamed("sum(sum(Initial Approvals))","h1bCount").filter(col("Skills") =!= "").withColumn("h1bCount",$"h1bCount"/8)
MLDL_h1b.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("data/MLDL_h1b")
// +------------+--------+                                                         
// |      Skills|h1bCount|
// +------------+--------+
// |     Pytorch|2038.875|
// |Scikit-learn| 1048.25|
// |  Tensorflow| 4391.25|
// +------------+--------+