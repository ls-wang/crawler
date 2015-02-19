package crawler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawl{
	public static Object lock = new Object();
	static Constant constant = new Constant();//initial properties Constant(讀properties檔)
//	static Set<String> sub_page_url = new HashSet();
	
	public static Document jsoupconnect (String url) throws NumberFormatException, InterruptedException{
		return jsoupconnect(url,Integer.valueOf(constant.getPara("connect_timeout").toString())*1000);
	}  
    public static Document jsoupconnect (String url,int timeout) throws InterruptedException{  
//		log.INFO(url);
    	Document doc=null;  
    	if(isIndexOf_domain(constant.getPara("domain_url") , url)==false){//檢查Domain不符
    		log.getLogger().warning("Domain 檢查不符");
    		return doc;  
    	}
        int retry=Integer.valueOf(constant.getPara("thread_fetch_time").toString());  
        while (null==doc && retry>0){  
            retry--;  
            try{  
            	Connection.Response response = Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36")
				.timeout(timeout)
				.ignoreHttpErrors(true) 
				.execute();
            	int statusCode = response.statusCode();
            	String statusMessage = response.statusMessage();
            	if(statusCode == 200) {
		//            doc= Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; rv:5.0)").timeout(timeout).get();  
            	    doc = Jsoup.connect(url)
            	    .userAgent("Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36")
            	    .timeout(timeout)
            	    .ignoreHttpErrors(true) 
            	    .get();
            	}
            	else {
            		log.getLogger().warning("received error code : " + statusCode + ", error Message :" + statusMessage);
            	}
            }catch(Exception e){  
//                e.printStackTrace(); //this work is displaying a InterruptedException 
                log.getLogger().warning("connect 獲取失敗啦,再重試"+retry+"次");  
//                Thread.sleep(1000*3);
                Thread.sleep(CommonFunc.getRandom(Integer.valueOf(constant.getPara("thread_retry_max_sec").toString())*1000));//隨機給時間,最大值thread_retry_max_sec
            }  
        }  
        return doc;  
    }	
    public static class Runnable_parse_main_page implements Runnable {
        private final CountDownLatch mDoneSignal;  
        private final String url;
      
        Runnable_parse_main_page(final CountDownLatch doneSignal, String Category)  
        {  
            this.mDoneSignal = doneSignal;  
            this.url = gen_url(Category);
        }  
    	 
    	@Override
		public void run() {
    		try {
    			Thread.sleep(CommonFunc.getRandom(Integer.valueOf(constant.getPara("thread_sleep_max_sec").toString())*1000));//隨機給時間,最大值thread_sleep_max_sec
				parse_main_page(url);
//				System.out.println(mDoneSignal.getCount());
				mDoneSignal.countDown();// 完成以後計數減一   
                // 計數為0時，主線程接觸阻塞，繼續執行其他任務   
//				System.out.println(mDoneSignal.getCount());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    public static class Runnable_parse_sub_page implements Runnable {
    	private final CountDownLatch mDoneSignal;  
    	private final String url;
    	
    	Runnable_parse_sub_page(final CountDownLatch doneSignal, String url) {
    		this.mDoneSignal = doneSignal;  
    		this.url = url;
    	}    	
    	
    	@Override
    	public void run() {
    		try {
    			Thread.sleep(CommonFunc.getRandom(Integer.valueOf(constant.getPara("thread_sleep_max_sec").toString()))*1000);//隨機給時間,最大值thread_sleep_max_sec
				parse_sub_page(url);
				mDoneSignal.countDown();// 完成以後計數減一   
                // 計數為0時，主線程接觸阻塞，繼續執行其他任務   
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
	public static void parse_main_page(String url) throws NumberFormatException, InterruptedException {
		log.getLogger().info(url);
		Set<String> Part_sub_page_url = new HashSet();
		
		Document doc = jsoupconnect(url);
		if (doc!=null){
			//get title_page link
			Elements story_hrefs = doc.select(".story div h4 a");
			for(Element href_story:story_hrefs){
				String sub_url_temp = href_story.absUrl("href");
//				System.out.println("sub_url_temp="+sub_url_temp);
				if(webcrawler.his_url.toString().indexOf(sub_url_temp) < 0)//compare with his_url, don't fetch the history url again
					Part_sub_page_url.add(sub_url_temp);
			}
			
			//get next page
			String next_url = "";
			next_url = doc.select("link[rel=next]").attr("href");
//			System.out.println("next_url="+next_url);
			if(!next_url.isEmpty())
				parse_main_page(next_url);
		}
			//return the result
			webcrawler.sub_page_url.addAll(Part_sub_page_url);
		
	} 	
	public static void parse_sub_page(String url) throws NumberFormatException, InterruptedException{
		String sPage_content = "";

		Document doc = jsoupconnect(url);
		if (doc!=null){
//		Document doc = Jsoup.connect(url).get();
			
			String sUurl  			= "";
			String sAuthor  		= "";
			String sProvider  		= "";
			String sTime  			= "";
			String sTitle  			= "";
			String sContent 		= "";
			String sCategory  		= "";
			String sPagecategory  	= "";
			int Category_size		= 0;
			//get content
			sUurl 			= url;
			sAuthor 		= doc.select("div cite span.fn").text();
			sProvider 		= doc.select("div cite span[class=provider org]").text();
			sTime 			= doc.select("div cite abbr").attr("title");
			sTitle 			= doc.select("div h1").text();
			sContent 		= doc.select("div.bd p").text();
			//避免out of index & NullPointerException
			Category_size 	= doc.select("div.bd a.path").size();
			if(Category_size >= 3)
				sCategory 		= doc.select("div.bd a.path").get(2).text();
			if(Category_size >= 4)
				sPagecategory 	= doc.select("div.bd a.path").last().text();			
//			sAuthor 		= doc.getElementById("mediaarticlehead").select("div cite span.fn").text();
//			sProvider 		= doc.getElementById("mediaarticlehead").select("div cite span[class=provider org]").text();
//			sTime 			= doc.getElementById("mediaarticlehead").select("div cite abbr").attr("title");
//			sTitle 			= doc.getElementById("mediaarticlehead").select("div h1").text();
//			sContent 		= doc.getElementById("mediaarticlebody").select("div.bd p").text();
//			sAuthor 		= get_text(doc, "mediaarticlehead", "div cite span.fn");
//			sProvider 		= get_text(doc, "mediaarticlehead", "div cite span[class=provider org]");
//			sTime 			= get_attr(doc, "mediaarticlehead", "div cite abbr", "title");
//			sTitle 			= get_text(doc, "mediaarticlehead", "div h1");
//			sContent 		= get_text(doc, "mediaarticlebody", "div.bd p");
//			//避免out of index & NullPointerException
//			Element div = doc.getElementById("mediaarticlemenutemp");
//			if(div != null){
//				Category_size 	= doc.getElementById("mediaarticlemenutemp").select("div.bd a.path").size();
//				if(Category_size >= 3)
//					sCategory 		= doc.getElementById("mediaarticlemenutemp").select("div.bd a.path").get(2).text();
//				if(Category_size >= 4)
//					sPagecategory 	= doc.getElementById("mediaarticlemenutemp").select("div.bd a.path").last().text();
//			}
//			System.out.println(sAuthor);
//			System.out.println(sProvider);
//			System.out.println(sTime);
//			System.out.println(sTitle);
//			System.out.println(sContent);
//			System.out.println(sCategory);
//			System.out.println(sPagecategory);
			sPage_content = sUurl.trim() 			+ "\t" + 
							sAuthor.trim() 			+ "\t" + 
							sProvider.trim() 		+ "\t" + 
							sTime.trim() 			+ "\t" + 
							sTitle.trim() 			+ "\t" + 
							sContent.trim() 		+ "\t" + 
							sCategory.trim() 		+ "\t" + 
							sPagecategory.trim() 	+ "\n";
			
//			log.getLogger().info(sPage_content);
//			System.out.println(sPage_content);//for test
//			return the result
			StringBuilder result = new StringBuilder();
			webcrawler.sOutPut =  result.append(webcrawler.sOutPut).append(sPage_content).toString();
		}		
	}	
//	public static void parse_page(String sLevel,String sUrl) throws NumberFormatException, InterruptedException{
//		String sPage_content = "";
//		
//		Document doc = jsoupconnect(sUrl);
//		if (doc!=null){
//			String[] output_ary = new String[20];
//			
//			String sUurl  			= "";
//			String sAuthor  		= "";
//			String sProvider  		= "";
//			String sTime  			= "";
//			String sTitle  			= "";
//			String sContent 		= "";
//			String sCategory  		= "";
//			String sPagecategory  	= "";
//			int Category_size		= 0;
//			//get content
//			sUurl 			= url;
//			sAuthor 		= doc.select("div cite span.fn").text();
//			sProvider 		= doc.select("div cite span[class=provider org]").text();
//			sTime 			= doc.select("div cite abbr").attr("title");
//			sTitle 			= doc.select("div h1").text();
//			sContent 		= doc.select("div.bd p").text();
//			//避免out of index & NullPointerException
//			Category_size 	= doc.select("div.bd a.path").size();
//			if(Category_size >= 3)
//				sCategory 		= doc.select("div.bd a.path").get(2).text();
//			if(Category_size >= 4)
//				sPagecategory 	= doc.select("div.bd a.path").last().text();			
//
//			sPage_content = sUurl.trim() 			+ "\t" + 
//							sAuthor.trim() 			+ "\t" + 
//							sProvider.trim() 		+ "\t" + 
//							sTime.trim() 			+ "\t" + 
//							sTitle.trim() 			+ "\t" + 
//							sContent.trim() 		+ "\t" + 
//							sCategory.trim() 		+ "\t" + 
//							sPagecategory.trim() 	+ "\n";
//			
////			log.getLogger().info(sPage_content);
////			System.out.println(sPage_content);//for test
////			return the result
//			StringBuilder result = new StringBuilder();
//			webcrawler.sOutPut =  result.append(webcrawler.sOutPut).append(sPage_content).toString();
//		}		
//	}	
	public static boolean isIndexOf_domain(String sDomain ,String url){
		if(url.indexOf(sDomain)>= 0){
			return true;
		}
		return false;
	}
	public static String gen_url(String sCategory){
		String url = "https://tw.news.yahoo.com/" + sCategory + "/archive/" + constant.getPara("start_page");
//		log.INFO(url);
		return url;
	}	
	
	public static Element check_ElementById(Document doc,String sId){
		return doc.getElementById(sId)!=null?doc.getElementById(sId):null;
	}
	public static Elements check_select(Element Emt,String st){
		return Emt.select(st)!=null?Emt.select(st):null;
	}
	public static String get_text(Document doc,String sId,String st){
		String stext = null;
		if(check_select(check_ElementById(doc,sId),st)!=null)
			stext = check_select(check_ElementById(doc,sId),st).text();
		return stext;
	}
	public static String get_attr(Document doc, String sId, String st, String sattr){
		String stext = null;
		if(check_select(check_ElementById(doc,sId),st)!=null)
			stext = check_select(check_ElementById(doc,sId),st).attr(sattr);
		return stext;
	}
}
