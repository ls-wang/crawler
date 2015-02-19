from scrapy.http import Request
from scrapy.http import HtmlResponse
from scrapy.contrib.spiders import CrawlSpider
from scrapy.selector import Selector
from scrapy import log
import string
import re
from yahootw.items import *

class YahootwSpider(CrawlSpider):
    name = "yahootw"
    domain_url = 'http://tw.news.yahoo.com'
    allowed_domains = ['tw.news.yahoo.com']
    catalog_file = '/tmp/catelog'
    categories = ['taipei']

    def start_requests(self):
        self.categories = [line.strip() for line in open(self.catalog_file)]
        for category in self.categories:
            yield Request('http://tw.news.yahoo.com/%s/archive/1.html' % category, self.parse_list, meta={'category':category})

    def parse_list(self, response):
        log.msg(response.url,level=log.WARNING)
        category = response.request.meta['category']
        hxs = Selector(response)
        urls = hxs.xpath('//div[contains(@class,"story")]/div[contains(@class,"txt")]/h4/a/@href').extract()
        for url in urls:
            yield Request(self.domain_url+url, self.parse_article,meta={'category':category})
        next = hxs.xpath('//ul[contains(@class,"future")]/li[1]/a[contains(@class,"yom-button")]/@href').extract()
        if len(next)>0:
            yield Request(self.domain_url+next[0], self.parse_list, meta={'category':category})

    def parse_article(self, response):
        item = ArticleItem()
        item['category'] = response.request.meta['category']
        item['url'] = response.url
        hxs = Selector(response)
        item['pagecategory'] = hxs.xpath('//div[@id="mediaarticlemenutemp"]/div[@class="bd"]/a[@class="path"][last()]/text()').extract()
        item['title'] = hxs.xpath('//div[@id="mediaarticlehead"]/div/h1/text()').extract()
        item['author'] = hxs.xpath('//div[@id="mediaarticlehead"]/div/cite/span[@class="fn"]/text()').extract()
        item['provider'] = hxs.xpath('//div[@id="mediaarticlehead"]/div/cite/span[@class="provider org"]/text()').extract()
        item['time'] = hxs.xpath('//div[@id="mediaarticlehead"]/div/cite/abbr/@title').extract()
        item['content'] = hxs.xpath('//div[@id="mediaarticlebody"]/div[@class="bd"]/p/text()').extract()
        return self.clear_list(item)

    def clear_list(self,object):
        if isinstance(object,ArticleItem):
            clear_props = ['title','author','provider','time','content','pagecategory']
        for name in clear_props:
            object[name] = self.extract_single(object[name])
        return object

    def extract_single(self, items):
        if len(items) == 0:
            return ""
        elif len(items) == 1:
            return items[0]
        return " ".join(items)

