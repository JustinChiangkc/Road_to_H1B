import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import spark.implicits._

val data = sc.textFile("salary_skill_joined2")

def parseSkills(text: String): String = {
    val bitmask = text.toInt
    var ret = new ListBuffer[String]()
    var a = 0;
    var (aws, bd, co, db, be, fe, dl) = (false, false, false, false, false, false, false)
    for (a <- 0 to 22){
        if (((1 << a) & bitmask) != 0){
            if (a == 0){
                if (!aws){
                    ret += "AWS"
                    aws = true
                }
            } else if (a <= 3){
                if (!bd){
                    ret += "Big Data"
                    bd = true
                }
            } else if (a <= 5){
                if (!co){
                    ret += "Container"
                    co = true
                }
            } else if (a <= 10){
                if (!db){
                    ret += "Database"
                    db = true
                }
            } else if (a <= 16){
                if (!be){
                    ret += "Back-end"
                    be = true
                }
            } else if (a <= 19){
                if (!fe){
                    ret += "Front-end"
                    fe = true
                }
            } else{
                if (!dl){
                    ret += "DL/ML"
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

