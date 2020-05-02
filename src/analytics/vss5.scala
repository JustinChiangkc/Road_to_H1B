import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import spark.implicits._

val data = sc.textFile("salary_skill_joined2")

def parseSkills(text: String): String = {
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
.save("salary_skill5.csv")
