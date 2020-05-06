from client import Squirtle
from client import getConfig

if __name__ == "__main__":
	config = getConfig()
	zeni = Squirtle(config)

	zeni.login()
	zeni.watergun()