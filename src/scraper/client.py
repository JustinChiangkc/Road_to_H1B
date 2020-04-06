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
		self.title = "Software Engineer"
		self.location = "New York"

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
		self._redirect_to_job_page()
		self._enter_search_info()

	def _redirect_to_job_page(self):
		try:
			link = self.driver.find_element_by_link_text('Jobs')
			link.click()
		except Exception as e:
			print('Cannot redirect to Jobs page')

	def _enter_search_info(self):
		# WebDriverWait(self.driver, 10).until(
		# 	EC.presence_of_element_located(
		# 		(By.ID, "job-search-box-keyword-id-ember405")
		# 	)
		# )
		time.sleep(3)
		title = self.driver.find_element_by_xpath("//input[starts-with(@id, 'jobs-search-box-keyword-id')]")
		title.clear()
		title.send_keys(self.title)
		location = self.driver.find_element_by_xpath("//input[starts-with(@id, 'jobs-search-box-location-id')]")
		location.click()
		location.send_keys(self.location)
		location.send_keys(Keys.RETURN)
		time.sleep(3)

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
	jenny = Squirtle(config)

	jenny.login()
	jenny.watergun()
	# jenny.quit()