import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import spark.implicits._

val data = sc.textFile("salary_skill_joined2")

def parseSkills(text: String): String = {
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

val parsed = data.map(line => line.split(",")).map(line => (line(0), parseSkills(line(1)).split(",")))
val splitted = parsed.flatMap{
    case (salary, skills) => for (skill <- skills) yield (skill, salary)
}
val filtered = splitted.filter(line => line._1 != "")
val grouped = filtered.groupByKey()
val df = grouped.map(x => (x._1, x._2.map(_.toLong).toSeq)).toDF("skill", "salary")
val df2 = df.withColumn("salary", explode($"salary"))

df2
.coalesce(1)
.write.format("com.databricks.spark.csv")
.option("header", "true")
.save("salary_skill2.csv")

