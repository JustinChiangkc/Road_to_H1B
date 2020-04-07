val mydata_original = sc.textFile("h1b_salary.txt")
val mydata_clean = sc.textFile("h1b_salary_clean.txt")

// the number of records - before clean
mydata_original.count() // res0: Long = 257905

// the number of records - after clean
mydata_clean.count() // res1: Long = 257904


// the number of records don't match
// reason : When I cleaned the data, I ignored the header.


// salary max & min
val salary_list = mydata_clean.map{x=>x.split(',')}.map{x=>(x(2).toInt)}
println("Highest Salary:" + salary_list.max())  // Highest Salary:998000
println("Lowest Salary:" + salary_list.min())  // Lowest Salary:1250