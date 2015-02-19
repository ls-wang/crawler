package crawler;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class webcrawler {
	
	static Constant constant = new Constant();//initial properties Constant(讀properties檔)
	static Set<String> sub_page_url = new HashSet<String>();
	static Set<String> his_url = new HashSet<String>();
	static String sOutPut = "";
	
	public static void main(String[] args) throws Exception {
		log.getLogger().info("!!!!!!!!start!!!!!!" + CommonFunc.getToday("yyyy/MM/dd HH:mm:ss"));
		//read history url list
		load_history_url();
		
		//crawl main page to extract sub title url
		crawl_main_page();
		
		//crawl sub page 
		crawl_sub_page();

		//output file
		String opf = constant.getPara("output_folder");
		String ofn = opf + CommonFunc.getToday_yyyyMMddHHmmss() + constant.getPara("output_file_name");
		FileOperate.File_Writer(ofn, sOutPut);
		FileOperate.File_Writer_Set(opf + CommonFunc.getToday_yyyyMMddHHmmss() + "_his_url.txt", sub_page_url);//for test
		
		//gz檔案
		String ogzn = opf + CommonFunc.getToday_yyyyMMddHHmmss() + constant.getPara("output_gzipFile_name");
		FileOperate.compressGzipFile(ofn,ogzn);
		
//		//刪除未壓縮的檔案
//		FileOperate.File_Delete();
		
		log.getLogger().info("!!!!!!!!finish!!!!!!" + CommonFunc.getToday("yyyy/MM/dd HH:mm:ss"));
	}

	public static void crawl_main_page() throws Exception {
		//set Total threads number
		int iTotal_thread = Integer.valueOf(constant.getPara("main_page_total_threads").toString());
		
		//解析Categories字串
		String[] Categories_Array = CommonFunc.splite_String(constant.getPara("Category"), ",");
		
		//ThreadPool monitor
		ExecutorService executor = Executors.newFixedThreadPool(iTotal_thread);  
		
		//thread Signal
		CountDownLatch doneSignal = new CountDownLatch(Categories_Array.length);
		
		for (int i = 0; i < Categories_Array.length; i++) {
			log.getLogger().info(Categories_Array[i]);
			executor.execute(new Crawl.Runnable_parse_main_page(doneSignal, Categories_Array[i]));  
		}
		//wait for all threads finish
        try  
        {  
        	doneSignal.await();// 等待所有threads结束   
        }  
        catch (InterruptedException e)  
        {  
            // TODO Auto-generated catch block   
            e.printStackTrace();  
        }  
        log.getLogger().info("All Threads have finished now.");  
//        log.getLogger().info("crawl_main_page:" + System.currentTimeMillis());  
        
		
		for(String Line: sub_page_url){
			log.getLogger().info(Line);
		}	
		
		//shut down pool
		executor.shutdown();
	}
	public static void crawl_sub_page() throws Exception {
		//set Total threads number
		int iTotal_thread = Integer.valueOf(constant.getPara("sub_page_total_threads").toString());
		
		//ThreadPool monitor
		ExecutorService executor = Executors.newFixedThreadPool(iTotal_thread);  
		
		//thread Signal
		CountDownLatch doneSignal = new CountDownLatch(sub_page_url.size());
		
		for (String sub_url:sub_page_url) {
//			log.getLogger().info(Categories_Array[i]);
//			log.getLogger().info(doneSignal.getCount());
			executor.execute(new Crawl.Runnable_parse_sub_page(doneSignal, sub_url));  
		}
		//wait for all threads finish
		try  
		{  
			System.out.println("sub_page_url次數-->"+ doneSignal.getCount());//for test
			doneSignal.await();// 等待所有threads结束   
		}  
		catch (InterruptedException e)  
		{  
			// TODO Auto-generated catch block   
			e.printStackTrace();  
		}  
		
		//shut down pool
		executor.shutdown();
	}

	public static void load_history_url(){
		
		String opf = constant.getPara("output_folder");
		//search output folder
		File[] File_list = FileOperate.FileSorter(opf);
		
		//get File name
		String File_name = constant.getPara("output_file_name").toString();
		
		//get files amount
		int iHis_url_loadback_count= Integer.valueOf(constant.getPara("load_his_url_amount").toString());
		
		//
		int index = 0;
        for (File f : File_list) {
        	if(f.toString().indexOf(constant.getPara("output_file_name"))>=0 && index < iHis_url_loadback_count){
        		log.getLogger().info(f.getName());
        		try {
        			his_url.addAll(FileOperate.File_Reader_GetSection(opf + f.getName(),"\t",0));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		index++;
//        		log.getLogger().info(Integer.toString(index));
        	}
        }
//		for(String Line: his_url){
//			log.getLogger().info(Line);
//		}        
	}
	
}
