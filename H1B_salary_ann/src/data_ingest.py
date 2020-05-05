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

# scrap job title starting with web developer, [data scientist, data analyst, data engineer] -> data
def scrap_title(job_title, yr):   # year as parameter
    a = job_title.lower().split()[0]
    b = job_title.lower().split()[1]
    year = yr
    r = urllib.request.urlopen('https://h1bdata.info/index.php?em=&job='+ a +'+'+ b +'*&city=&year={}'.format(year))   # all jobs starting with "software"
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

def scrap_all_years(job_title):

    records = []
    for year in range(2012, 2020, 1):
        if job_title == 'software engineer':
            yr_data = scrap_sw_all(year)
        else:
            yr_data = scrap_title(job_title, year)
        records.append(yr_data)
    
    data = pd.concat(records, ignore_index=True)
    return data


def save_to_txt(input_data, output_file) :   
    input_data.to_csv(output_file, sep=',')  # cannot use space as delimiter


def save_to_csv(input_data, output_file):
    input_data.to_pickle(output_file)


# main
if __name__ == "__main__":
    
    swe = scrap_all_years('software engineer')
    wd = scrap_all_years('web developer')
    de = scrap_all_years('data engineer')
    ds = scrap_all_years('data scientist')
    da = scrap_all_years('data analyst')
     
    all_titles = [swe, wd, de, ds, da]
    data = pd.concat(all_titles, ignore_index=True) # Concatenating the dataframes [like union], index from 0 ~ n-1

    save_to_txt(data, 'h1b_salary.txt')
    save_to_csv(data, 'h1b_salary_df')
    



# commands upload file to dumbo & hdfs
# scp ./h1b_salary.txt mt4050@dumbo.es.its.nyu.edu:/home/mt4050
# hdfs dfs -put /home/mt4050/h1b_salary.txt
