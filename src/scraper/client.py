from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
import random
import time
import csv

class Squirtle:
	def __init__(self, conf):
		self.driver = webdriver.Chrome(conf['path_to_chromedriver'])
		self.username = conf['username']
		self.password = conf['password']
		self.titles = ["Software Engineer", "Machine Learning Engineer",
					   "Data Scientist", "Data Analyst", "Web Developer"]
		self.title_acronyms = ['SE','MLE', 'DS', 'DA', 'WD']

		self.locations = ["San Francisco Bay Area", "New York City Metropolitan Area",
						  "San Diego, California, United States", "Los Angeles Metropolitan Area",
						  "Seattle, Washington, United States", "Greater Boston",
						"Denver Metropolitan Area", "Greater Chicago Area"]
		self.location_acronyms = ['San Francisco', 'New York', 'San Diego', 'Los Angeles',
								  'Seattle', 'Boston', 'Denver', 'Chicago']

		self.fp = None
		self.pages = 10

	def login(self):
		self.driver.get("https://www.linkedin.com/uas/login")
		elem = self.driver.find_element_by_id("username")
		elem.send_keys(self.username)
		elem = self.driver.find_element_by_id("password")
		elem.send_keys(self.password)
		elem.send_keys(Keys.RETURN)
		time.sleep(3)

	def quit(self):
		self.driver.quit()

	def watergun(self):
		try:
			for title, title_acr in zip(self.titles, self.title_acronyms):
				for location, location_acr in zip(self.locations, self.location_acronyms):
					self.current_title = title
					self.current_location = location

					self._redirect_to_job_page()
					self.fp = open('./results/{}_{}.csv'.format(title_acr, location_acr), 'w')
					self.csvwriter = csv.writer(self.fp)
					self.csvwriter.writerow(('title', 'company', 'location', 'job description'))
					# self._enter_search_info(title, location)
					self._shoot()
					self.fp.close()
		except Exception as e:
			raise e
		finally:
			self.fp.close()
			self.quit()

	def _redirect_to_job_page(self, page=1):
		# try:
		# 	link = self.driver.find_element_by_link_text('Jobs')
		# 	link.click()
		# except Exception as e:
		# 	print('Cannot redirect to Jobs page')
		# 	raise e
		title = self.current_title
		location = self.current_location
		url = "https://www.linkedin.com/jobs/search/?keywords=" + title + "&location=" + location + "&start="+str(page)
		self.driver.get(url)
		time.sleep(3)

	def _enter_search_info(self, title, location):
		WebDriverWait(self.driver, 120).until(
			EC.element_to_be_clickable(
				(By.XPATH, "//input[starts-with(@id, 'jobs-search-box-keyword-id')]")
			)
		)

		WebDriverWait(self.driver, 120).until(
			EC.element_to_be_clickable(
				(By.XPATH, "//input[starts-with(@id, 'jobs-search-box-location-id')]")
			)
		)
		time.sleep(1)
		elem = self.driver.find_element_by_xpath("//input[starts-with(@id, 'jobs-search-box-keyword-id')]")
		elem.clear()
		elem.send_keys(title)
		elem = self.driver.find_element_by_xpath("//input[starts-with(@id, 'jobs-search-box-location-id')]")
		elem.click()
		elem.send_keys(location)
		time.sleep(2)
		elem.send_keys(Keys.RETURN)
		time.sleep(3)

	def _shoot(self):
		page = 1
		count = 0
		for _ in range(self.pages):
			print('Current Page: {}'.format(page))

			try:
				WebDriverWait(self.driver, 60).until(
					EC.visibility_of_element_located(
						(By.XPATH, "//ul[starts-with(@class, 'jobs-search-results__list')]")
					)
				)
			except Exception as e:
				break

			li_locator_base = "//ul[starts-with(@class, 'jobs-search-results__list')]/li[{}]"

			i = 1
			while i < 26:
				print('# of page: {}, # of tab: {}'.format(page, i))
				try:
					li_locator = li_locator_base.format(i)
					WebDriverWait(self.driver, 60).until(
						EC.presence_of_element_located(
							(By.XPATH, li_locator)
						)
					)
					li = self.driver.find_element_by_xpath(li_locator)
					coordinates = li.location_once_scrolled_into_view
					self.driver.execute_script('window.scrollTo({}, {});'.format(coordinates['x'], coordinates['y']))

					locator = ".//a[starts-with(@class, 'job-card-search__link')]"
					WebDriverWait(li, 60).until(
						EC.presence_of_element_located(
							(By.XPATH, locator)
						)
					)
					tab = li.find_element_by_xpath(locator)
					tab.click()

					title = self._parse_title(li)
					company = self._parse_company(li)
					location = self._parse_location(li)
					jd = self._parse_job_description()
				except Exception as e:
					# raise e
					break
				else:
					i += 1
					count += 1

					fields = (title, company, location, jd)
					self.csvwriter.writerow(fields)
					time.sleep(random.uniform(1.5, 2.0))

			page += 1
			try:
				self._redirect_to_job_page(page)
			except Exception as e:
				print('Last page has been reached')
				break
			else:
				print('Total tabs: {}'.format(count))
			# try:
			# 	xpath = "//button[@aria-label='Page {}']".format(page)
			# 	elem = self.driver.find_element_by_xpath(xpath)
			# except Exception as e:
			# 	print('Last page has been reached')
			# 	break
			# else:
			# 	print('Total tabs: {}'.format(count))
			# 	elem.click()


	def _parse_title(self, li):
		# locator = ".//a[starts-with(@class, 'job-card-search__link')]"
		locator = "//a[starts-with(@class, 'jobs-details-top-card__job-title-link')]"
		WebDriverWait(self.driver, 60).until(
			EC.visibility_of_element_located(
				(By.XPATH, locator)
			)
		)
		# elem = li.find_element_by_xpath(locator)
		elem = self.driver.find_element_by_xpath(locator)
		title = elem.text
		return title.encode("utf8")

	def _parse_company(self, li):
		locator = ".//a[starts-with(@class, 'job-card-search__company-name-link')]"
		WebDriverWait(li, 60).until(
			EC.presence_of_element_located(
				(By.XPATH, locator)
			)
		)
		elem = li.find_element_by_xpath(locator)
		company = elem.text
		return company.encode("utf8")

	def _parse_location(self, li):
		locator = ".//span[starts-with(@class, 'job-card-search__location')]"
		WebDriverWait(li, 60).until(
			EC.presence_of_element_located(
				(By.XPATH, locator)
			)
		)
		elem = li.find_element_by_xpath(locator)
		location = elem.text
		return location.encode("utf8")

	def _parse_job_description(self):
		WebDriverWait(self.driver, 60).until(
			EC.visibility_of_element_located(
				(By.ID, "job-details")
			)
		)

		# job description
		elem = self.driver.find_element_by_xpath("//div[@id='job-details']/span")
		jd = elem.text
		return jd.encode("utf8")

def getConfig():
	config = {}
	with open('./linkedinconfig.txt', 'r') as myfile:
		lines = myfile.read().splitlines()

	# linkedinconfig.txt is in the format of A: B
	# ex. username: jw5865
	for line in lines:
		x, y = line.split(':')
		config[x.rstrip()] = y.lstrip()
	return config

if __name__ == "__main__":
	config = getConfig()
	zeni = Squirtle(config)

	zeni.login()
	zeni.watergun()