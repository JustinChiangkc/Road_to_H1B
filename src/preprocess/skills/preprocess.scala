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

def getC(text: String): Boolean = {
    val lower = text.toLowerCase()
    if (lower.indexOf("c/c\\+\\+") != -1){
        return true
    }
    val idx1 = lower.indexOf("c")
    val idx2 = lower.indexOf("c++")
    return !(idx1 == -1 || idx1 == idx2 || idx1 - 1 >= 0 && lower(idx1-1) != ' ' || idx1 + 1 < lower.length && lower(idx1+1) != ' ')
}

def getCpp(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("c\\+\\+") != -1
}

def getPython(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("python") != -1
}

def getJava(text: String): Boolean = {
    val lower = text.toLowerCase()
    val idx1 = text.indexOf("java")
    val idx2 = text.indexOf("javascript")
    return idx1 != -1 && idx1 != idx2
}

def getJavascript(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("javascript") != -1
}

def getGo(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("go") != -1
}

def getPhp(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("php") != -1
}

def getScala(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("scala") != -1
}

def getCsharp(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("c#") != -1
}

def getRuby(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("ruby") != -1
}

def getSql(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("sql") != -1
}

def getCompany(text: String): String = {
    return text.replaceAll(",", "").replaceAll("inc", "").replaceAll("Inc", "").trim
}

val file = "jobs"

val raw = sc.textFile(file)

val raw_no_header = raw.mapPartitionsWithIndex {(idx, iter) => if (idx == 0) iter.drop(1) else iter }

val splitted = raw_no_header.map(line => line.split("-\\+\\*-"))

val cleaned = splitted.map(line => ((getCompany(line(1)), getTitle(line(0))), (getC(line(3)), getCpp(line(3)), getPython(line(3)), getJava(line(3)), getJavascript(line(3)), getGo(line(3)), getScala(line(3)), getCsharp(line(3)), getSql(line(3)))))

val merged = cleaned.reduceByKey((x, y) => (x._1 | y._1, x._2 | y._2, x._3 | y._3, x._4 | y._4, x._5 | y._5, x._6 | y._6, x._7 | y._7, x._8 | y._8, x._9 | y._9))

val unpacked = merged.map({case(x, y) => (x._1, x._2, y._1, y._2, y._3, y._4, y._5, y._6, y._7, y._8, y._9)})

unpacked.saveAsTextFile("cleaned_jobs")
