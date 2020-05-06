import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LinearRegressionModel
import org.apache.spark.mllib.optimization.L1Updater
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.regression.LinearRegressionWithSGD
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import scala.collection.mutable.ListBuffer

val data = sc.textFile("salary_skill_joined")

def parseSkills(text: String): String = {
    val bitmask = text.toInt 
    var ret = new ListBuffer[Int]()
    for (a <- 0 to 8){
        if (((1 << a) & bitmask) != 0){
            ret += 1
        } else {
            ret += 0
        }
    }
    ret = ret.reverse
    return ret.mkString(",")
}

val parsedData = data.map{line =>
    val fields = line.split(',')
    LabeledPoint(fields(0).toDouble, Vectors.dense(parseSkills(fields(1)).split(",").map(_.toDouble)))
}

val splits = parsedData.randomSplit(Array(0.8, 0.2), seed=11L)
val training = splits(0).cache()
val test = splits(1)

val iter = 1000
val step = 0.001

val model = LinearRegressionWithSGD
model.optimizer.setNumIterations(iter).setRegParam(0.1).setUpdater(new L1Updater)
model.run(training)

val eval = test.map{ point =>
    val pred = model.predict(point.features)
    (pred, point.label)
}

val MSE = eval.map{ case(v, p) => math.pow((v - p), 2)}.mean()

println(s"training MSE $MSE")

val metrics = new MulticlassMetrics(eval)
val accuracy = metrics.accuracy
println(s"Accuracy = $accuracy")
