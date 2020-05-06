# 個人：新增註解 add state
# 整合 -  excel 畫圖 --> skill - salary, company -salary 「加入州」

import matplotlib. pyplot as plt
import pandas as pd
import numpy as np
import seaborn as sns


def new_title(row):
    title = row['job title']
    if title.startswith('SOFTWARE'):
        return "SOFTWARE ENGINEER"
    elif title.startswith('WEB DEVELOPER'):
        return "WEB DEVELOPER"
    elif title.startswith('DATA ENGINEER'):
        return "DATA ENGINEER"
    elif title.startswith('DATA SCIENTIST'):
        return "DATA SCIENTIST"
    elif title.startswith('DATA ANALYST'):
        return "DATA ANALYST" 


def add_new_title(df):    # only data analyst
    df["job title - clean"] = df.apply(new_title, axis = 1)  # axis = 1 -> applying to col

def application_numbers_compare(df):
    plot = sns.catplot(data=df,kind='count',x='year',hue='job title - clean')
    plt.show()

def salary_compare(state):
    plot = sns.catplot(data=df,kind='box',x='year',y = 'base salary',hue='job title - clean')
    plt.show()

def save_to_pickle(input_data, output_file):
    input_data.to_pickle(output_file)

def save_to_csv(input_data, output_file):
    input_data.to_csv(output_file)


# main
if __name__ == "__main__":
    
    # load dataframe
    df = pd.read_pickle('h1b_salary_df')
    # identify job title
    df_clean = df
    add_new_title(df_clean)
    save_to_pickle(df_clean, 'h1b_salary_df_clean')

    save_to_csv(df_clean, 'h1b_salary_df_clean.csv')
    # plots
    add_new_title(df_clean)
    application_numbers_compare(df_clean)
    salary_compare(df_clean)

