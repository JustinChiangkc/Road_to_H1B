import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer 
import spark.implicits._

val data = sc.textFile("salary_skill_joined")

// decode PLS
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
.save("salary_skill.csv")
