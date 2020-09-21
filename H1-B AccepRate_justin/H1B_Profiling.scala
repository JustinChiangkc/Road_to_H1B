//Read Cleaned data
val dir = "final_project/data/cleaned_data/part-00000-a892c595-ab0b-4543-87a5-e3979c02151c-c000.csv"
val cdata = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv(dir)

//Read rawaw data
// val df = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/")


df.count
//res67: Long = 622684     

//dropnull
//res12: Long = 622350

//dropduplicate
//res24: Long = 616164  

//Remove state typo
//res34: Long = 616163

cdata.count
//res66: Long = 616163   


cdata.show
// +-----------+--------------------+-----------------+---------------+--------------------+------------------+-----+-----+
// |Fiscal Year|            Employer|Initial Approvals|Initial Denials|Continuing Approvals|Continuing Denials|NAICS|State|
// +-----------+--------------------+-----------------+---------------+--------------------+------------------+-----+-----+
// |       2009|BROOKLINE PUBLIC ...|                0|              0|                   1|                 0|   61|   MA|
// |       2009| ASPEN TECHNLOGY INC|                1|              0|                   1|                 0|   54|   MA|
// |       2009|GUARDIAN EDGE TEC...|                2|              0|                   1|                 0|   51|   CA|
// |       2009|         WEIDBEE INC|                0|              1|                   0|                 0|   54|   TX|
// |       2009|         BHRIGUS INC|                0|              4|                   0|                 1|   54|   NJ|
val cdataSQL = cdata.withColumnRenamed("Fiscal Year","Year").withColumnRenamed("Initial Approvals","IA").withColumnRenamed("Initial Denials","ID").withColumnRenamed("Continuing Approvals","CA").withColumnRenamed("Continuing Denials","CD")
cdataSQL.persist
cdataSQL.createOrReplaceTempView("cdataSQL_view")
spark.sql("SELECT COUNT(DISTINCT(Employer)) FROM cdataSQL_view").show()
// +------------------------+                                                      
// |count(DISTINCT Employer)|
// +------------------------+
// |                  285719|
// +------------------------+
spark.sql("SELECT DISTINCT(Year) FROM cdataSQL_view order by Year desc").show()
// +----+
// |Year|
// +----+
// |2019|
// |2018|
// |2017|
// |2016|
// |2015|
// |2014|
// |2013|
// |2012|
// |2011|
// |2010|
// |2009|
// +----+
spark.sql("SELECT COUNT(DISTINCT(NAICS)) FROM cdataSQL_view").show()

// +---------------------+                                                         
// |count(DISTINCT NAICS)|
// +---------------------+
// |                   26|
// +---------------------+

spark.sql("SELECT COUNT(DISTINCT(State)) FROM cdataSQL_view").show()
// +---------------------+                                                         
// |count(DISTINCT State)|
// +---------------------+
// |                   58|
// +---------------------+

spark.sql("SELECT min(IA),min(ID),min(CA),min(CD) FROM cdataSQL_view").show()
// +-------+-------+-------+-------+
// |min(IA)|min(ID)|min(CA)|min(CD)|
// +-------+-------+-------+-------+
// |      0|      0|      0|      0|
// +-------+-------+-------+-------+

spark.sql("SELECT max(IA),max(ID),max(CA),max(CD) FROM cdataSQL_view").show()
// +-------+-------+-------+-------+
// |max(IA)|max(ID)|max(CA)|max(CD)|
// +-------+-------+-------+-------+
// |   9191|   3070|  26145|   3910|
// +-------+-------+-------+-------+

spark.sql("SELECT sum(IA)/(sum(ID)+sum(IA)) as InitialApprovalRate, sum(CA)/(sum(CD)+sum(CA)) as ContinueApprovalRate FROM cdataSQL_view").show()
// +-------------------+--------------------+
// |InitialApprovalRate|ContinueApprovalRate|
// +-------------------+--------------------+
// | 0.8858667671430667|  0.9406996803775941|
// +-------------------+--------------------+
