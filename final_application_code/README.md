# Team Name : GoodJob! 


## Acknowledgments 
	Thanks to Eberly College of Science - Penn State for sharing the course content of Probability Theory and Mathematical Statistics, which allows we to reference when measured our linear regression model. [https://online.stat.psu.edu/stat414/node/226/]

## Tools
    a. Scala Spark
    b. Spark SQL
    c. Spark mllib
    d. Tableau

## For salary-rate linear regression model & hypothesis testing : Ann Tsai
    
    a. data ingest : 
        run "python3 final_application_code/data_ingest/salary_data_ingest.py" in command line

    b. etl : 
        go to final_application_code/etl_code/salary_etl.scala, and execute the code in spark REPL

    c. profiling : 
        go to final_application_code/profiling_code/salary_profiling.scala, and execute the code in spark REPL

    d. analytics : 
        i. get the median of salary 
            -> go to final_application_code/app_code/salary_get_salary_median.scala, and execute the code in spark REPL
            
        ii. get the result of linear equation and hypothesis
            -> go to final_application_code/app_code/salary_linear_regression_model.scala, and execute the code in spark REPL

    e. result : 
        see final_application_code/screenshots/salary_linear_regression_result.png

## For pl/skills vs salary : Jiancheng Wang
    a. data ingest:
	run "python3 final_application_code/data_ingest/scraper/main.py" in command line
	requires selenium installed. requires to put linkedin credential in linkedinconfig.txt.
	It will automatically scraped from linkedin. Built based on early April's website layer
	
    b. etl
	go to final_application_code/eta_code/pl_etl.scala, and execute code in spark REPL
	It will generate the ((company_name, role), (pl_bitmap, degree_bitmap))

	go to final_application_code/etl_code/skills_etl.scala, and execute code in spark REPL
	It will generate the ((company_name, role), (skills_bitmap, degree_bitmap))

    c. Profiling
	first, go to final_application_code/etl_code/join.scala, and execute codes in spark REPL
	It will join the salary and pl/skills based on (company_name, role)

	then go to final_application_code/profiling_code/visualization_pl_skills, and execute all codes in spark REPL
	It will generate the data after sorting into different categories. They are ready to be visualized on Tableau

    d. Analytics
	go to final_application_code/eta_code/pl_importance_test.scala, and execute code in spark REPL
	It attempts to build a linear regression model for importance test but failed
	
	Most analytics with aggregate methods(ex.distinct, count, median, average) are done on tableau

    e, Result
	You can find several screenshots of the final result under final_application_code/screenshots/visualization_pl_skills

## for pl/skills vs H1B : Ko-Chun, Chiang
    a. data ingest:
        First, follow the instrction in "Ingest h1b data" to scp the "h1b_cleaned_data" and "cleaned_jobs" to Dumbo.
        Secondly, use Linux commend line in "Ingest h1b data", which will build a directory and put the data on HDFS.
    b. Profiling
        run "H1B_Profiling.scala" in profileing_code to understand the data.

    c. ETL:
        i. Extract data for h1b acceptance for time, NSICS, Employer analysis.
        Run "H1B_Acceptance_ETL.scala" using REPL and get H1B_pivot_Employer_year, H1B_pivot_state_year, H1B_pivot_NAICS_year, and H1B_AR_Year_NAICS_Employer for Tableau visualization.
    d. Application:
        i. Get the relation between programming language and h1b counts.
        Run "h1b_PL.scala" in app_code in REPL. The code will join "cleaned_jobs" where contains processed skills data from Linkedin dataset and "h1b_cleaned_data". The result will produce a csv file, "PL_h1b", contains information for h1b counts per programming language.

        ii. Get the relation between technical skills and h1b counts.
        Run "h1b_skills.scala" in app_code in REPL. The code will join "cleaned_jobs" where contains processed skills data from Linkedin dataset and "h1b_cleaned_data". The result will produce several csv files, "MLDL_h1b","frontend_h1b", "backend_h1b", "DB_h1b", "Docker_h1b2", "BD_h1b2", "CC_h1b2", and "Skills_h1b", contains information for h1b counts in different skills.

## H1B acceptance rate data preparation for Salary_H1B analysis : Ko-Chun, Chiang
    a. Run "getH1bAcceptanceRateforSalary.scala" on REPL to get "AR_Year_Employer_State". 



