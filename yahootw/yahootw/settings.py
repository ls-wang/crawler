# Scrapy settings for yahootw project
#
# For simplicity, this file contains only the most important settings by
# default. All the other settings are documented here:
#
#     http://doc.scrapy.org/topics/settings.html
#

BOT_NAME = 'yahootw'

SPIDER_MODULES = ['yahootw.spiders']
NEWSPIDER_MODULE = 'yahootw.spiders'

DOWNLOAD_DELAY = 2
# Crawl responsibly by identifying yourself (and your website) on the user-agent
#USER_AGENT = 'yahootw (+http://www.yourdomain.com)'
STAT_DUMP = False
LOG_ENABLED = True
LOG_LEVEL = 'WARNING'
