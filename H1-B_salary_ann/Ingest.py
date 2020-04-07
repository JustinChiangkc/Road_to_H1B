# scrapping  H1-B Salary
import urllib.request
from bs4 import BeautifulSoup
import pandas as pd


# wild card - 'software engineer', 'software developer', 'software programmer', 'software engineer 1', 'software development engineer',......
def scrap_sw_all(yr):   # year as parameter

    year = yr
    r = urllib.request.urlopen('https://h1bdata.info/index.php?em=&job=software*&city=&year={}'.format(year))   # all jobs starting with "software"
    soup = BeautifulSoup(r, "html.parser")  # to get rid of warning
    data2 = soup.find_all('tr')

    labels = []
    for h in data2[0].find_all('th'):
        labels.append(h.get_text().strip().lower())
       
    final = []
    for data in data2[1:]:
        data_list = []
        for d in data.find_all('td'):
            d_str = d.get_text().replace(',','')
            
            if d_str.isnumeric():
                data_list.append(int(d_str))
            else:
                data_list.append(d_str)                      
        final.append(data_list)
     
    df = pd.DataFrame(final, columns = labels)    
    df['submit date'] = pd.to_datetime(df['submit date'])
    df['start date'] = pd.to_datetime(df['start date'])
    df['state'] = df['location'].str.split().str[-1] 
    df['year'] = df['submit date'].dt.year
    df['month'] = df['submit date'].dt.month
    return df



def save_to_txt(input_data, output_file) :   
    input_data.to_csv(output_file, sep=',')  # cannot use space as delimiter


# main
if __name__ == "__main__":
    
    records = []  
    for year in range(2012, 2020, 1):
        yr_data = scrap_sw_all(year)
        records.append(yr_data)  # all jobs starting with "software"   
    
    data = pd.concat(records, ignore_index=True) # Concatenating the dataframes [like union], index from 0 ~ n-1
    save_to_txt(data, 'h1b_salary.txt')



# commands upload file to dumbo & hdfs
# scp ./h1b_salary.txt mt4050@dumbo.es.its.nyu.edu:/home/mt4050
# hdfs dfs -put /home/mt4050/h1b_salary.txt