// normalize company name
// return String: normalized_result
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

def processTitle(text: String): String ={
    var lower = text.toLowerCase()
    if (lower == "machine learning engineer"){
        lower = "data engineer"
    }
    return lower
}

val skills = "cleaned_jobs2"
val salary = "salary.txt"

val skillRdd = sc.textFile(skills)
val salaryRdd = sc.textFile(salary)

val salary_cleaned = salaryRdd.map(line => line.split(",")).map(line => ((processCompany(line(0)), processTitle(line(1))), (line(2), line(3), line(4), line(5), line(6))))
val skill_cleaned = skillRdd.map(line => line.replace("(", "").replace(")", "").split(",")).map(line => ((processCompany(line(0)), processTitle(line(1))), (line(2), line(3))))

val joined = salary_cleaned.join(skill_cleaned).map {
    case ((company, title), ((salary, status, state, year, time), (skills, degree))) => salary + "," + skills
}

joined.saveAsTextFile("salary_skill_joined2")
