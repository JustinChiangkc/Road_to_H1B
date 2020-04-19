
//scp h1b_datahubexport-09to19.zip kc4200@dumbo.es.its.nyu.edu:/home/kc4200/final_project
//unzip h1b_datahubexport-09to19.zip 
//hdfs dfs -mkdir final_project
//hdfs dfs -mkdir final_project/data
//hdfs dfs -put h1b* final_project/data

//Read data as dataframe
val df = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/")

//count rows
df.count
//res7: Long = 622684  

//drop columns which dosn't need
val df2 = df.drop("Tax ID","City", "ZIP")

//drop rows with null
val df3 = df2.na.drop()

//count rows
df3.count
//res12: Long = 622350

// Convert String to IntegerType, and replace "," to "" before that. Ex: 2,344 (string)-> 2344 (int)
import org.apache.spark.sql.types.IntegerType
val df4 = df3.withColumn("Initial Approvals",regexp_replace($"Initial Approvals", "," ,"")).withColumn("Initial Approvals",col("Initial Approvals").cast(IntegerType)).withColumn("Initial Denials",regexp_replace($"Initial Denials", "," ,"")).withColumn("Initial Denials",col("Initial Denials").cast(IntegerType)).withColumn("Continuing Approvals",regexp_replace($"Continuing Approvals", "," ,"")).withColumn("Continuing Approvals",col("Continuing Approvals").cast(IntegerType)).withColumn("Continuing Denials",regexp_replace($"Continuing Denials", "," ,"")).withColumn("Continuing Denials",col("Continuing Denials").cast(IntegerType))

//Check duplicate and found there are duplicate (df4.count = 622350)
df4.distinct.count
//res24: Long = 616164  
val df5 = df4.dropDuplicates


//Count distinct for Fiscal Year,Employer,NAICS,State
df5.select(countDistinct("Fiscal Year")).show
// +---------------------------+                                                   
// |count(DISTINCT Fiscal Year)|
// +---------------------------+
// |                         11|
// +---------------------------+

df5.select(countDistinct("Employer")).show
// +------------------------+                                                      
// |count(DISTINCT Employer)|
// +------------------------+
// |                  285721|
// +------------------------+

df5.select(countDistinct("NAICS")).show
// +---------------------+                                                         
// |count(DISTINCT NAICS)|
// +---------------------+
// |                   26|
// +---------------------+

df5.select(countDistinct("State")).show
// +---------------------+                                                         
// |count(DISTINCT State)|
// +---------------------+
// |                   60|
// +---------------------+

df5.dropDuplicates("State").select("State").collect
//res6: Array[org.apache.spark.sql.Row] = Array([AZ], [SC], [LA], [MN], [NJ], [DC], 
    // [OR], [VA], [RI], [KY], [WY], [NH], [MI], [NV], [WI], [ID], [CA], [CT], [NE], 
    // [MT], [NC], [VT], [MD], [DE], [MO], [VI], [IL], [ME], [GU], [WA], [ND], [MS], 
    // [AL], [IN], [AE], [?], [OH], [TN], [IA], [NM], [PA], [SD], [NY], [FM], [TX], 
    // [WV], [NEW YORK], [GA], [MA], [KS], [FL], [CO], [AK], [AR], [OK], [PR], [AP], 
    // [MP], [UT], [HI])

//NEW YORK to NY and remove [?]
val df6 = df5.withColumn("State", when(df5("State") === "NEW YORK", "NY").otherwise(df5("State"))).filter((df5("State").rlike("[A-Z][A-Z]")))

df6.count
//res30: Long = 616163
df6.dropDuplicates("State").select("State").collect

df6.coalesce(1).write.option("header","true").option("sep",",").format("csv").save("final_project/data/cleaned_data")




