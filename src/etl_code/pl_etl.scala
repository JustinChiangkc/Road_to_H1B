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
    
    val pattern1 = """.*(c/c\+\+).*""".r
    val pattern2 = """.*[\s,]+(c)[\s,]+.*""".r
    val pattern3 = """^(c)[\s,]+.*""".r
    val pattern4 = """.*[\s,]+(c)$""".r
    
    lower match{
        case pattern1(_) => return true
        case pattern2(_) => return true
        case pattern3(_) => return true
        case pattern4(_) => return true       
        case _ => return false
    }
}


def getCpp(text: String): Boolean = {
    val lower = text.toLowerCase()
    
    val pattern1 = """.*(c/c\+\+).*""".r
    val pattern2 = """.*([\s,]*c\+\+[\s,]*).*""".r
    val pattern3 = """^(c\+\+)[\s,]+.*""".r
    val pattern4 = """.*[\s,]+(c\+\+)$""".r
    
    lower match{
        case pattern1(_) => return true
        case pattern2(_) => return true
        case pattern3(_) => return true
        case pattern4(_) => return true
        case _ => return false
    }
}

def getPython(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("python") != -1
}

def getJava(text: String): Boolean = {
    val lower = text.toLowerCase()
    val pattern1 = """.*(java)(?!script).*""".r
    
    lower match{
        case pattern1(_) => return true
        case _ => return false
    }    
}

def getJavascript(text: String): Boolean = {
    val lower = text.toLowerCase()
    return lower.indexOf("javascript") != -1
}

def getGo(text: String): Boolean = {
    val pattern1 = """.*([Gg]o[Ll]ang).*""".r
    val pattern2 = """.*[\s,]+(Go)[\s,]+""".r
    
    text match{
        case pattern1(_) => return true
        case pattern2(_) => return true
        case _ => return false
    }
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

// extract degree requirement from job description
// return Int: bitmap
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

// preprocess company name
def getCompany(text: String): String = {
    return text.replaceAll(",", "").replaceAll("inc", "").replaceAll("Inc", "").trim
}

// extract pls from job description
// return Int: bitmap
def getSkills(text: String): Int = {
    var bitmask: Int = 0
    
    if (getC(text)){
        bitmask |= (1 << 0)
    }
    
    if (getCpp(text)){
        bitmask |= (1 << 1)
    }
    
    if (getPython(text)){
        bitmask |= (1 << 2)
    }
    
    if (getJava(text)){
        bitmask |= (1 << 3)
    }
    
    if (getJavascript(text)){
        bitmask |= (1 << 4)
    }
    
    if (getGo(text)){
        bitmask |= (1 << 5)
    }
    
    if (getScala(text)){
        bitmask |= (1 << 6)
    }
    
    if (getCsharp(text)){
        bitmask |= (1 << 7)
    }
    
    if (getSql(text)){
        bitmask |= (1 << 8)
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

unpacked.saveAsTextFile("cleaned_jobs")
