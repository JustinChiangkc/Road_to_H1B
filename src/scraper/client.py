from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
import time

class Squirtle:
	def __init__(self, conf):
		self.driver = webdriver.Chrome(conf['path_to_chromedriver'])
		self.username = conf['username']
		self.password = conf['password']
		self.titles = ["Software Engineer", "Software Developer", "Machine Learning Engineer",
					   "Data Engineer", "Data Analyst", "Web Developer"]

		self.locations = ["New York City Metropolitan Area", "San Francisco Bay Area",
						  "San Diego, California, United States", "Los Angeles Metropolitan Area",
						  "Seattle, Washington, United States", "Greater Boston",
						"Denver Metropolitan Area", "Greater Chicago Area"]

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
			self._redirect_to_job_page()
			for title in self.titles:
				for location in self.locations:
					self._enter_search_info(title, location)
					self._shoot()
					break
		except Exception as e:
			# self.quit()
			raise e

	def _redirect_to_job_page(self):
		try:
			link = self.driver.find_element_by_link_text('Jobs')
			link.click()
		except Exception as e:
			print('Cannot redirect to Jobs page')
			raise e

	def _enter_search_info(self, title, location):
		WebDriverWait(self.driver, 60).until(
			EC.element_to_be_clickable(
				(By.XPATH, "//input[starts-with(@id, 'jobs-search-box-location-id')]")
			)
		)
		elem = self.driver.find_element_by_xpath("//input[starts-with(@id, 'jobs-search-box-keyword-id')]")
		elem.clear()
		elem.send_keys(title)

		elem = self.driver.find_element_by_xpath("//input[starts-with(@id, 'jobs-search-box-location-id')]")
		elem.click()
		elem.send_keys(location)
		elem.send_keys(Keys.RETURN)
		time.sleep(3)

	def _shoot(self):
		page = 1
		results = []
		while True:
			print('Current Page: {}'.format(page))

			WebDriverWait(self.driver, 60).until(
				EC.element_to_be_clickable(
					(By.XPATH, "//ul[starts-with(@class, 'jobs-search-results__list')]")
				)
			)
			ul = self.driver.find_element_by_xpath("//ul[starts-with(@class, 'jobs-search-results__list')]")
			lis = ul.find_elements_by_tag_name("li")
			for li in lis:
				li.click()

				WebDriverWait(self.driver, 60).until(
					EC.visibility_of_element_located(
						(By.ID, "job-details")
					)
				)

				elem = self.driver.find_element_by_xpath("//div[@id='job-details']/span")
				results.append(elem.text)

			page += 1
			try:
				xpath = "//button[@aria-label='Page {}']".format(page)
				elem = self.driver.find_element_by_xpath(xpath)
			except Exception as e:
				print('Last page has been reached')
			else:
				elem.click()

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
	# jenny.quit()