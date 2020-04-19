from collections import Counter, defaultdict
import pandas as pd
import seaborn
import matplotlib.pyplot as plt
import os

if __name__ == '__main__':
	data = []
	dirpath = '../../../data/skills/cleaned_jobs'
	for file in os.listdir(dirpath):
		with open(os.path.join(dirpath, file), 'r') as myFile:
			lines = myFile.read().splitlines()
		data.extend([line[1:-1].split(',') for line in lines])

	scounter = defaultdict(lambda: Counter())
	dcounter = defaultdict(lambda: Counter())

	jobs = ['Software Engineer', 'Web Developer', 'Machine Learning Engineer', 'Data Scientist', 'Data Analyst']
	skill_set = ['C', 'C++', 'Python', 'Java', 'Javascript', 'Go', 'Scala', 'C#', 'SQL']
	degree_set = ['BS', 'MS', 'PHD']


	for row in data:
		title = row[1]
		skills = int(row[2])
		for i in range(len(skill_set)):
			if skills & (1 << i):
				scounter[title][skill_set[i]] += 1
		degree = int(row[3])
		for i in range(len(degree_set)):
			if degree & (1 << i):
				dcounter[title][degree_set[i]] += 1

	df_dict = {skill: [scounter[job][skill] for job in jobs] for skill in skill_set}
	df_dict['Jobs'] = jobs
	df = pd.DataFrame(df_dict)
	fig, ax1 = plt.subplots(figsize=(10, 10))
	tidy = df.melt(id_vars='Jobs').rename(columns=str.title)
	seaborn.barplot(x='Jobs', y='Value', hue='Variable', data=tidy, ax=ax1)
	seaborn.despine(fig)
	plt.show()

	df_dict = {degree: [dcounter[job][degree] for job in jobs] for degree in degree_set}
	df_dict['Jobs'] = jobs
	df = pd.DataFrame(df_dict)
	fig, ax1 = plt.subplots(figsize=(10, 10))
	tidy = df.melt(id_vars='Jobs').rename(columns=str.title)
	seaborn.barplot(x='Jobs', y='Value', hue='Variable', data=tidy, ax=ax1)
	seaborn.despine(fig)
	plt.show()