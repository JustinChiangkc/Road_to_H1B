def getAWS(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("aws") != -1
}

def getAzure(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("azure") != -1
}

def getGCP(text: String): Boolean = {
    var lower = text.toLowerCase()
    val pattern1 = """.*(gcp).*""".r
    val pattern2 = """.*(google[\s]*cloud).*""".r
    
    lower match{
        case pattern1(_) => return true
        case pattern2(_) => return true
        case _ => return false  
    }
}

def getKafka(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("kafka") != -1
}

def getES(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("elasticsearch") != -1
}

def getSpark(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("spark") != -1
}

def getHadoop(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("hadoop") != -1
}

def getMapReduce(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("mapreduce") != -1
}

def getDocker(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("docker") != -1
}

def getK8S(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("k8s") != -1 || lower.indexOf("kubernetes") != -1
}

def getJenkins(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("jenkins") != -1
}

def getMaven(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("maven") != -1
}

def getPostgre(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("postgres") != -1
}

def getMySQL(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("mysql") != -1
}

def getHive(text:String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("hive") != -1
}

def getMongoDB(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("mongo") != -1
}

def getCassandra(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("cassandra") != -1
}

def getRedis(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("redis") != -1
}

def getHBase(text:String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("hive") != -1
}

def getNet(text: String): Boolean = {
    var lower = text.toLowerCase()
    val pattern = """.*(\.net).*""".r
    lower match{
        case pattern(_) => return true
        case _ => return false  
    }
}

def getDjango(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("django") != -1
}

def getFlask(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("flask") != -1
}

def getSpring(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("spring") != -1
}

def getNodeJS(text: String): Boolean = {
    var lower = text.toLowerCase()
    val pattern = """.*(node[\.]?js).*""".r
    lower match{
        case pattern(_) => return true
        case _ => return false  
    }
}

def getExpressJS(text: String): Boolean = {
    var lower = text.toLowerCase()
    val pattern = """.*(express[\.]?js).*""".r
    lower match{
        case pattern(_) => return true
        case _ => return false  
    }
}

def getAngularJS(text: String): Boolean = {
    var lower = text.toLowerCase()
    val pattern = """.*(angular[\.]?[js]?).*""".r
    lower match{
        case pattern(_) => return true
        case _ => return false  
    }
}

def getVueJS(text: String): Boolean = {
    var lower = text.toLowerCase()
    val pattern = """.*(vue[\.]?[js]?).*""".r
    lower match{
        case pattern(_) => return true
        case _ => return false  
    }
}

def getReact(text: String): Boolean = {
    var lower = text.toLowerCase()
    val pattern1 = """.*(react[\.]?js).*""".r
    val pattern2 = """.*(react[\s]+native).*""".r

    lower match{
        case pattern1(_) => return true
        case pattern2(_) => return true
        case _ => return false  
    }
}

def getPytorch(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("pytorch") != -1
}

def getTensorflow(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("tensorflow") != -1
}

def getMXNet(text: String): Boolean = {
    var lower = text.toLowerCase()
    return lower.indexOf("mxnet") != -1
}

def getCompany(text: String): String = {
    return text.replaceAll(",", "").replaceAll("inc", "").replaceAll("Inc", "").trim
}

def getTitle(rawtitle: String): String = {
    val sde = """.*(software).*""".r
    val da = """.*(analy).*""".r
    val ds = """.*(scientist).*""".r
    val web = """.*(developer).*""".r
    val mle = """.*(machine).*""".r
    val lower = rawtitle.toLowerCase()
    lower match{
        case sde(_) => return "Software Engineer"
        case da(_) => return "Data Analyst"
        case ds(_) => return "Data Scientist"
        case web(_) => return "Web Developer"
        case mle(_) => return "Machine Learning Engineer"
        case _ => return "None"
    }
}

def getSkills(text: String): Int = {
    var bitmap: Int = 0
    
    if (getAWS(text)){
        bitmap |= (1 << 0)
    }
    if (getAzure(text)){
        bitmap |= (1 << 1)
    }
    if (getGCP(text)){
        bitmap |= (1 << 2)
    }
    if (getKafka(text)){
        bitmap |= (1 << 3)
    }
    if (getES(text)){
        bitmap |= (1 << 4)
    }
    if (getSpark(text)){
        bitmap |= (1 << 5)
    }
    if (getHadoop(text)){
        bitmap |= (1 << 6)
    }
    if (getMapReduce(text)){
        bitmap |= (1 << 7)
    }
    if (getDocker(text)){
        bitmap |= (1 << 8)
    }
    if (getK8S(text)){
        bitmap |= (1 << 9)
    }
    if (getJenkins(text)){
        bitmap |= (1 << 10)
    }
    if (getMaven(text)){
        bitmap |= (1 << 11)
    } 
    if (getPostgre(text)){
        bitmap |= (1 << 12)
    }
    if (getMySQL(text)){
        bitmap |= (1 << 13)
    }
    if (getHive(text)){
        bitmap |= (1 << 14)
    }
    if (getMongoDB(text)){
        bitmap |= (1 << 15)
    }
    if (getCassandra(text)){
        bitmap |= (1 << 16)
    }
    if (getRedis(text)){
        bitmap |= (1 << 17)
    }
    if (getHBase(text)){
        bitmap |= (1 << 18)
    }
    if (getNet(text)){
        bitmap |= (1 << 19)
    }
    if (getDjango(text)){
        bitmap |= (1 << 20)
    }
    if (getFlask(text)){
        bitmap |= (1 << 21)
    }
    if (getSpring(text)){
        bitmap |= (1 << 22)
    }
    if (getNodeJS(text)){
        bitmap |= (1 << 23)
    }
    if (getExpressJS(text)){
        bitmap |= (1 << 24)
    }
    if (getAngularJS(text)){
        bitmap |= (1 << 25)
    }
    if (getVueJS(text)){
        bitmap |= (1 << 26)
    }
    if (getReact(text)){
        bitmap |= (1 << 27)
    }
    if (getPytorch(text)){
        bitmap |= (1 << 28)
    }
    if (getTensorflow(text)){
        bitmap |= (1 << 29)
    }
    if (getMXNet(text)){
        bitmap |= (1 << 30)
    }
    
    return bitmap
}

def getDegree(text: String): Int = {
    val lower = text.toLowerCase()
    val bs1 = """.*[^a-zA-Z]+(b\.*[as]\.*|bachelor['’s]*)[\s,\./]+.*""".r
    val bs2 = """^(b\.*[as]\.*|bachelor['’s]*)[\s,\./]+.*""".r
    val bs3 = """.*[^a-zA-Z]+(b\.*[as]\.*|bachelor['’s]*)$""".r

    val ms1 = """.*[^a-zA-Z]+(m\.*[s]\.*|m\.*eng|master['’s]*)[\s,\./]+.*""".r
    val ms2 = """^(m\.*[s]\.*|m\.*eng|master['’s]*)[\s,\./]+.*""".r
    val ms3 = """.*[^a-zA-Z]+(m\.*[s]\.*|m\.*eng|master['’s]*)$""".r

    val phd = """.*(ph[\.]*d).*""".r

    var bitmask: Int = 0

    lower match {
        case bs1(_) => bitmask |= (1 << 0)
        case bs2(_) => bitmask |= (1 << 0)
        case bs3(_) => bitmask |= (1 << 0)
        case _ => bitmask |= (0 << 0)
    }

    lower match {
        case ms1(_) => bitmask |= (1 << 1)
        case ms2(_) => bitmask |= (1 << 1)
        case ms3(_) => bitmask |= (1 << 1)
        case _ => bitmask |= (0 << 1)
    }

    lower match {
        case phd(_) => bitmask |= (1 << 2)
        case _ => bitmask |= (0 << 2)
    }

    return bitmask
}

val file = "jobs"
val raw = sc.textFile(file)
val raw_no_header = raw.mapPartitionsWithIndex {(idx, iter) => if (idx == 0) iter.drop(1) else iter }
val splitted = raw_no_header.map(line => line.split("-\\+\\*-"))
val cleaned = splitted.map(line => ((getCompany(line(1)), getTitle(line(0))), (getSkills(line(3)), getDegree(line(3)))))
val merged = cleaned.reduceByKey((x, y) => (x._1 | y._1, x._2 | y._2))
val unpacked = merged.map({case(x, y) => (x._1, x._2, y._1, y._2)})
unpacked.saveAsTextFile("cleaned_jobs2")
