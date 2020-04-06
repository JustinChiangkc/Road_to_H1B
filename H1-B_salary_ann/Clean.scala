// scp ./h1b_salary_clean.txt mt4050@dumbo.es.its.nyu.edu:/home/mt4050
// hdfs dfs -put /home/mt4050/h1b_salary_clean.txt

import scala.io.Source
import java.io._
import scala.io.BufferedSource


object Clean {

    def main(args:Array[String]){  
        parse("h1b_salary.txt") 
    }  

    def resource(filename: String): BufferedSource = {
        val source = Source.fromFile(filename)
        return source
    }

    def split_advanced(delimiter:String, line:String): Array[String] = {
        return line.split(delimiter)
    }

    def output(employer: String, job_title: String, salary: String, visa_status: String, state: String, year: String, month: String): String = {
        val log =  employer + "," + job_title + "," + salary + "," + visa_status + "," + state + "," + year + "," + month
        return log
    }

    def parse(input: String): Unit = {

        val file = new File("h1b_salary_clean.txt")
        val bw = new BufferedWriter(new FileWriter(file))

        // Load the dataset
        val source = resource(input)
        
        for (line <- source.getLines.drop(1)) { // remove header
            
            val delimiter = "," // return String
            val arr = split_advanced(delimiter, line)
            
            // Filter out any records which do not parse correctly - hint: each record should have exactly 11 values.
            val len = arr.length
            if (len == 11){

                // Extract the columns we need
                val employer = arr(1)
                val job_title = "SOFTWARE ENGINEER"
                val salary = arr(3)
                val visa_status = arr(7)
                val state = arr(8)
                val year = arr(9)
                val month = arr(10)

                
                // Save the extracted data to txt file
                val log = output(employer, job_title, salary, visa_status, state, year, month)
                bw.write(log+"\n") 
            }
        }
        bw.close()
        source.close()
    }
}
