///////////////////////////  clean job title //////////////////////////////

def replace_title(title: String): String = {
    if (title startsWith "SOFTWARE") return "SOFTWARE ENGINEER"
    else if (title startsWith "WEB DEVELOPER") return"WEB DEVELOPER"
    else if (title startsWith "DATA ENGINEER") return "DATA ENGINEER"
    else if (title startsWith "DATA SCIENTIST") return "DATA SCIENTIST"
    else if (title startsWith "DATA ANALYST") return "DATA ANALYST"
    "NONE"
}


///////////////////////////  parse company name ///////////////////////////     hw 11 project code drop # 2 updated

def parse_company(text: String): String = {
    var lower = text.toLowerCase()
    var symbols = """[\.,-/\*^@#&$%!\?]""".r
    lower = symbols.replaceAllIn(lower, "")
    
    var paren = """\(.*\)""".r
    lower = paren.replaceAllIn(lower, "")
   
    //val suffix = """(inc)|(llc)|(limited)|((in)?corporat((e[d]?)|(ion)))|(corp[^a-z]?)|(company)|(lp)|(ltd)|(university)|(associate[s]?)|(com)|(co)|(group)|(holding)|(lab[s]?)""".r
    val suffix = """(inc)|(llc)|(limited)|(incorporation)|(incorporated)|(corporate)|(corporation)|(orporated)|(company)|(lp)|(ltd)|(university)|(associate[s]?)|( com )|( co )|( corp )|( com,)|( co,)|( corp,)|(group)|(holding)|(lab[s]?)""".r
    lower = suffix.replaceAllIn(lower, "") 

    val services = """(service[s]?)|(online)|(system[s]?)|(solution[s]?)|(data)|(software)|(infotech)|(digital)|([\s,]info[\s,])|(soft)|(web)|""".r
    lower = services.replaceAllIn(lower, "")
    
    val industry = """(marketing)|(consulting)|(financial)|(technolog(y|(ies)))|(communication[s]?)|(entertainment)|([\s,]it[$\s,])|(information)|(business)|(network[s]?)|(resource[s]?)""".r
    lower = industry.replaceAllIn(lower, "")

    val location = """(international)|(global)|(america[s]?)|([,\s]us[,\s])|(usa)""".r
    lower = location.replaceAllIn(lower, "")
    return lower.trim() // returns the stated string after removing all the white spaces.
}
 
/////////////////////////////////////////////////////////////////////////


//// PARSE SALARY DATA

//val raw = sc.textFile("/user/mt4050/h1b_salary.txt")
val raw = sc.textFile("/user/mt4050/project_data/h1b_salary.txt")

// drop the header
val raw_no_header = raw.mapPartitionsWithIndex {(idx, iter) => if (idx == 0) iter.drop(1) else iter }
// raw_no_header.take(10).foreach(println)

// extract the fields we want
// (employer, job_title, base_salary, visa_status, state, year, month)
val raw_split = raw_no_header.map(line => ((line.split(','))(1), (line.split(','))(2), (line.split(','))(3), (line.split(','))(7), (line.split(','))(8), (line.split(','))(9), (line.split(','))(10)))
// raw_split.take(10).foreach(println)


// identify job title  & parse company name 
val job = raw_split.map(x => (parse_company(x._1), replace_title(x._2), x._3, x._4, x._5, x._6, x._7))   // hw 11 project code drop # 2 updated
// job.take(10).foreach(println)

// filter len != 7
val output = job.map{case(employer, job_title, base_salary, visa_status, state, year, month) => employer + "," + job_title+ "," + base_salary+ "," + visa_status+ "," + state+ "," + year+ "," + month}
val filter_output = output.filter(line => (line.split(',')).length == 7)
// filter_output.take(10).foreach(println)


//// PARSE H1B - ACCEPTANCE RATE DATA

// dataframe parse
val acceptance_raw = sc.textFile("/user/mt4050/project_data/h1b_acceptance_rate.csv")  // original file
val acceptance_raw_no_header = acceptance_raw.mapPartitionsWithIndex {(idx, iter) => if (idx == 0) iter.drop(1) else iter }

// filter len != 6
val acceptance_raw_no_header_filter = acceptance_raw_no_header.filter(line => (line.split(",")).length == 6)
val acceptance_raw_split = acceptance_raw_no_header_filter.map(line => ((line.split(','))(0), (line.split(','))(1), parse_company((line.split(','))(2)), (line.split(','))(5)))


// filter len != 4
val acceptance_raw_output = acceptance_raw_split.map{case(yr, state, company, rate) => yr + "," + state+ "," + company+ "," + rate}
val acceptance_raw_filter_output = acceptance_raw_output.filter(line => (line.split(",")).length == 4)


/*
scala> acceptance_raw.count
res11: Long = 399951

scala> acceptance_raw_filter_output.count
res12: Long = 227329
*/


// acceptance_raw_split.take(10).foreach(println)
/*
(2012,NJ,cognizant tech solnscorp,0.9382590060210225)
(2012,MD,tata consultancy,0.9622737055323458)
(2014,MD,tata consultancy,0.9345468146116445)
(2013,MD,tata consultancy,0.9614923584057536)
(2013,TX,infosys,0.9332361516034985)
(2012,TX,infosys,0.9744036218004527)
(2013,NJ,cognizant  solnscorp,0.8456026058631921)
(2015,MD,tata consultancy svcs,0.9438603451696091)
(2012,NJ,wipro,0.9762757385854969)
(2014,TX,cognizant tech corp,0.9378372544787394)
*/



///////////////////////////  save files  /////////////////////////////////     hw 11 project code drop # 2 updated

// save to txtfile                      
var repartitioned = filter_output.repartition(1)
repartitioned.saveAsTextFile("/user/mt4050/project_data/h1b_salary_clean.txt")


// save to csv
import org.apache.spark.sql.types.{StructType, StructField, StringType}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import sqlContext.implicits._


// Salary

// Defining schema
val header_raw = (raw.take(1)(0).split(",")).drop(1)   
//Array(employer, job title, base salary, location, submit date, start date, case status, state, year, month)  
//raw_no_header.take(10).foreach(println)
val header = Array(header_raw(0), header_raw(1), header_raw(2), header_raw(6), header_raw(7), header_raw(8) , header_raw(9))
val schema = StructType(header.map(fieldName => StructField(fieldName,StringType, true)))
// filter_output.take(5).foreach(println)

// Converting String RDD to Row RDD
val rowRDD = filter_output.map(_.split(",")).map(x => Row(x(0), x(1), x(2), x(3), x(4), x(5) , x(6)))
// rowRDD.take(5).foreach(println)
// [level 3,SOFTWARE ENGINEER,96900,WITHDRAWN,CO,2012,7]


val sqlContext = new org.apache.spark.sql.SQLContext(sc)
val df = sqlContext.createDataFrame(rowRDD, schema)

df.show(5)
/*
+------------------+-----------------+-----------+-----------+-----+----+-----+
|          employer|        job title|base salary|case status|state|year|month|
+------------------+-----------------+-----------+-----------+-----+----+-----+
|           level 3|SOFTWARE ENGINEER|      96900|  WITHDRAWN|   CO|2012|    7|
|            amazon|SOFTWARE ENGINEER|     115000|  WITHDRAWN|   WA|2012|    8|
|stmicroelectronics|SOFTWARE ENGINEER|     110820|  WITHDRAWN|   CA|2012|    7|
|     contentactive|SOFTWARE ENGINEER|      51979|  WITHDRAWN|   TX|2012|    4|
|             xerox|SOFTWARE ENGINEER|      74196|  WITHDRAWN|   NY|2012|    4
*/

// Writing dataframe to a file with overwrite mode, header and single partition.
df.repartition(1).write.mode ("overwrite").format("com.databricks.spark.csv").option("header", "true").save("/user/mt4050/project_data/h1b_salary_df_clean.csv")



// H1B
// Defining schema
val accp_header = Array("FiscalYear", "State", "Employer", "ApprovalRate")
val accp_schema = StructType(accp_header.map(fieldName => StructField(fieldName,StringType, true)))

// content

val accp_rowRDD = acceptance_raw_filter_output.map(_.split(",", -1)).map(x => Row(x(0), x(1), x(2), x(3)))
accp_rowRDD.take(2).foreach(println)


val sqlContext = new org.apache.spark.sql.SQLContext(sc)
val accp_df = sqlContext.createDataFrame(accp_rowRDD, accp_schema)

accp_df.show(5)

/*
Fiscal Year|State|            Employer|      ApprovalRate|
+-----------+-----+--------------------+------------------+
|       2012|   NJ|cognizant tech so...|0.9382590060210225|
|       2012|   MD|    tata consultancy|0.9622737055323458|
|       2014|   MD|    tata consultancy|0.9345468146116445|
|       2013|   MD|    tata consultancy|0.9614923584057536|
|       2013|   TX|             infosys|0.9332361516034985|
*/

accp_df.repartition(1).write.mode ("overwrite").format("com.databricks.spark.csv").option("header", "true").save("/user/mt4050/project_data/h1b_acceptance_rate_clean")

// hdfs dfs -get h1b_salary_clean.txt
// scp -r mt4050@dumbo.es.its.nyu.edu:/home/mt4050 ./project_data