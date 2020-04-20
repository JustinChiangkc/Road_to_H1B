import collections

if __name__ == '__main__':
	with open('../../data/salary/h1b_salary_clean.txt', 'r', encoding="utf-8") as myFile:
		lines = myFile.read().splitlines()


	counter = collections.defaultdict(lambda : collections.Counter())

	for line in lines:
		company = line.split(',')[0]
		company = company.lower()

		words = [w for w in company.split(' ') if w ]

		for i in range(-1, -5, -1):
			if -1 * i <= len(words):
				counter[i][words[i]] += 1

	for k, v in counter.items():
		for t in v.most_common(40):
			print('{}: {}'.format(k, t))
