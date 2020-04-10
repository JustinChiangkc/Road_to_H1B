import csv
import os

if __name__ == '__main__':
	for file in os.listdir('./results'):
		with open(os.path.join('./results', file), 'r') as rfile:
			with open(os.path.join('./results_txt', file.rstrip('csv')+'txt'), 'w') as wfile:
				reader = csv.reader(rfile)

				fields = next(reader)
				wfile.write('-+*-'.join(fields)+'\n')

				for line in reader:
					nline = [l.lstrip("b'").rstrip("'") for l in line]
					wfile.write('-+*-'.join(nline)+'\n')
