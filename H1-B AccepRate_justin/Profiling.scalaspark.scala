//Read Cleaned data
val dir = "final_project/data/cleaned_data/part-00000-a892c595-ab0b-4543-87a5-e3979c02151c-c000.csv"
val cdata = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv(dir)

//Read rawaw data
val df = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/")


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

val EE = spark.read.options(Map("inferSchema"->"true","delimiter"->",","header"->"true")).csv("final_project/data/Employer")
